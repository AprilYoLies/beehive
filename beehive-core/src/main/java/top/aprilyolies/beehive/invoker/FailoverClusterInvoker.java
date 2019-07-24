package top.aprilyolies.beehive.invoker;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import top.aprilyolies.beehive.cluster.loadbalance.LoadBalance;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.registry.Registry;
import top.aprilyolies.beehive.registry.ZookeeperRegistry;
import top.aprilyolies.beehive.transporter.client.Client;

import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.aprilyolies.beehive.common.UrlConstants.*;
import static top.aprilyolies.beehive.common.UrlConstants.PATH_SEPARATOR;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public class FailoverClusterInvoker<T> extends AbstractInvoker {
    private final URL url;
    // 缓存的 invokers 信息
    private volatile List<Invoker<T>> invokers;
    // 缓存注册中心信息
    private volatile Registry registry;
    // 最大重复 invoke 次数
    private final int MAX_REINVOKE_TIMES = 5;
    // 记录当前重复 invoke 的次数
    private final ThreadLocal<Integer> retryCountThreadLocal = new RetryCountThreadLocal();
    // 更新 invokers 的标志，在有服务上线或者下线后，此标志会设置为 true
    private volatile boolean needUpdateInvokers = false;
    // 用于缓存当前的 providers，invokers 将会根据此 providers 来构建
    private volatile List<String> providers;
    // 保存到当前线程中的 load balance
    private final ThreadLocal<LoadBalance> loadBalanceThreadLocal = new LoadBalanceThreadLocal();

    public FailoverClusterInvoker(URL url) {
        this.url = url;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) {
        try {
            if (this.invokers == null || registry == null) {
                synchronized (this) {
                    if (invokers == null || registry == null) {
                        invokers = listInvokers();
                        Registry registry = BeehiveContext.unsafeGet(REGISTRIES, Registry.class);
                        addInvokersRefreshListener(registry);
                        this.registry = registry;
                    }
                }
            }
            if (needUpdateInvokers) {
                updateInvokers();
            }
            int reInvokeCount = retryCountThreadLocal.get();
            LoadBalance loadBalance = loadBalanceThreadLocal.get();
            if (loadBalance == null) {
                loadBalance = createLoadBalance(url);
                loadBalanceThreadLocal.set(loadBalance);
            }
            // 选择合适的 invoker
            Invoker<T> invoker = selectInvoker(loadBalance, invokers);
            if (invoker != null) {
                Invoker chain = buildInvokerChain(invoker);
                Object result = chain.invoke(info);
                if (result == null) {
                    // 辅助刷新 invokers
                    needUpdateInvokers = true;
                    // 返回结果为空进行重试
                    if (reInvokeCount++ < MAX_REINVOKE_TIMES) {
                        retryCountThreadLocal.set(reInvokeCount);
                        return doInvoke(info);
                    } else {
                        // 重试达到上限，直接返回 null 结果
                        throw new IllegalStateException("Do invoke " + MAX_REINVOKE_TIMES + " times, but the result was still null");
                    }
                } else return result;
            } else {
                throw new IllegalStateException("There is none of service provider could be use, please check your " +
                        "registry center that if there is any service has published.");
            }
        } finally {
            retryCountThreadLocal.set(0);
        }
    }

    /**
     * 更新 invokers 信息
     */
    @SuppressWarnings("unchecked")
    private synchronized void updateInvokers() {
        if (!needUpdateInvokers) return;
        List<Invoker<T>> oldInvokers = this.invokers;
        List<Invoker<T>> newInvokers = new ArrayList<>();
        List<String> oldProviders = this.providers;
        Map<String, List<String>> providersMap = BeehiveContext.unsafeGet(UrlConstants.PROVIDERS, Map.class);
        List<String> newProviders = providersMap.get(url.getParameter(UrlConstants.SERVICE));
        newProviders = filterProviders(newProviders);
        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        // 获取将要被移除的 provider 集合
        for (String oldProvider : oldProviders) {
            if (!newProviders.contains(oldProvider)) {
                toRemove.add(oldProvider);
            }
        }
        // 获取将要被添加的 provider 集合
        for (String newProvider : newProviders) {
            if (!oldProviders.contains(newProvider)) {
                toAdd.add(newProvider);
            }
        }
        // 将旧的 invokers 中没有被移除的 invoker 添加到新 invokers 中
        for (Invoker<T> oldInvoker : oldInvokers) {
            if (oldInvoker instanceof RemoteInvoker) {
                RemoteInvoker remoteInvoker = (RemoteInvoker) oldInvoker;
                String provider = remoteInvoker.getProvider();
                if (!toRemove.contains(provider)) {
                    newInvokers.add(remoteInvoker);
                }
            }
        }
        oldInvokers.clear();
        List<Invoker<T>> invokers = buildInvokers(toAdd);
        newInvokers.addAll(invokers);
        // 重新缓存 providers 和 invokers 信息
        this.providers = newProviders;
        this.invokers = newInvokers;
        needUpdateInvokers = false;
    }

    /**
     * 根据指定的 providers 构建 invokers
     *
     * @param providers
     * @return
     */
    private List<Invoker<T>> buildInvokers(List<String> providers) {
        Client client = BeehiveContext.unsafeGet(UrlConstants.CONSUMERS_TRANSPORT, Client.class);
        assert providers != null;
        return createRemoteInvoker(providers, client);
    }

    /**
     * 添加 invokers 刷新监听器
     *
     * @param regsitry
     */
    private void addInvokersRefreshListener(Registry regsitry) {
        try {
            if (regsitry instanceof ZookeeperRegistry) {
                ZookeeperRegistry zkRegistry = (ZookeeperRegistry) regsitry;
                CuratorFramework client = zkRegistry.getClient();
                String registryPath = getProviderPath(url);
                PathChildrenCache pathCache = new PathChildrenCache(client, registryPath, true);
                pathCache.start();
                pathCache.getListenable().addListener(new InvokersRefreshListener(pathCache));
            }
        } catch (Exception e) {
            logger.error("Can't add invokers refresh listener");
            e.printStackTrace();
        }
    }

    /**
     * 获取 provider 路径信息
     *
     * @param url
     * @return
     */
    private String getProviderPath(URL url) {
        String group = url.getParameterElseDefault(GROUP_KEY, DEFAULT_GROUP);
        String service = url.getParameter(SERVICE);
        String category = PROVIDERS;
        StringBuilder sb = new StringBuilder(PATH_SEPARATOR).
                append(group).
                append(PATH_SEPARATOR).
                append(service).
                append(PATH_SEPARATOR).
                append(category);
        return sb.toString();
    }

    private Invoker<T> selectInvoker(LoadBalance loadBalance, List<Invoker<T>> invokers) {
        if (invokers.isEmpty()) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return loadBalance.select(invokers);
    }

    private LoadBalance createLoadBalance(URL url) {
        return loadBalanceSelector.createLoadBalance(url);
    }

    private synchronized List<Invoker<T>> listInvokers() {
        //noinspection unchecked
        Map<String, List<String>> providersMap = BeehiveContext.unsafeGet(UrlConstants.PROVIDERS, Map.class);
        List<String> providers = providersMap.get(url.getParameter(UrlConstants.SERVICE));
        providers = filterProviders(providers);
        this.providers = providers;
        Client client = BeehiveContext.unsafeGet(UrlConstants.CONSUMERS_TRANSPORT, Client.class);
        assert providers != null;
        return createRemoteInvoker(providers, client);
    }

    /**
     * 过滤不符合条件的 provider，如 protocol 不一致，serializer 不一致，service 不一致
     *
     * @param providers
     * @return
     */
    private List<String> filterProviders(List<String> providers) {
        List<String> validProviders = new ArrayList<>();
        for (String provider : providers) {
            String decodedProvider = URLDecoder.decode(provider);
            String providerInfo = provider2ProviderInfo(decodedProvider);
            URL url = URL.buildFromAddress(providerInfo);
            boolean isValid = true;
            isValid = isValid && isServiceMatch(this.url, url);
            isValid = isValid && isProtocolMatch(this.url, url);
            isValid = isValid && isSerializerMatch(this.url, url);
            if (isValid)
                validProviders.add(provider);
        }
        return validProviders;
    }

    /**
     * 根据全限定名的 provider 串获取到实际的 provider 信息
     * 传入 /beehive/top.aprilyolies.service.BeehiveService/providers/beehive://192.168.1.102:7442?protocol=beehive&proxyFactory=jdk&service=top.aprilyolies.service.BeehiveService&serializer=hessian&id=demoService&serverPort=7442
     * 返回 beehive://192.168.1.102:7442?protocol=beehive&proxyFactory=jdk&service=top.aprilyolies.service.BeehiveService&serializer=hessian&id=demoService&serverPort=7442
     *
     * @param provider 包含路径信息的全限定 provider 信息
     * @return 去掉路径信息的实际的 provider 信息
     */
    private String provider2ProviderInfo(String provider) {
        int idx = provider.indexOf("://");
        String path = provider.substring(0, idx);
        int i = path.lastIndexOf("/");
        return provider.substring(i + 1);
    }

    private boolean isSerializerMatch(URL ref, URL target) {
        return ref.getParameter(UrlConstants.SERIALIZER).equals(target.getParameter(UrlConstants.SERIALIZER));
    }

    /**
     * 协议是否匹配
     *
     * @param ref
     * @param target
     * @return
     */
    private boolean isProtocolMatch(URL ref, URL target) {
        return ref.getParameter(UrlConstants.SERVICE_PROTOCOL).equals(target.getProtocol());
    }

    /**
     * 服务是否匹配
     *
     * @param ref
     * @param target
     * @return
     */
    private boolean isServiceMatch(URL ref, URL target) {
        return ref.getParameter(UrlConstants.SERVICE).equals(target.getParameter(UrlConstants.SERVICE));
    }

    /**
     * 根据 providers 构建 Invoker 信息
     *
     * @param providers 从注册中心获取的 provider 信息
     * @param client    数据交互的客户端
     * @return
     */
    private List<Invoker<T>> createRemoteInvoker(List<String> providers, Client client) {
        List<Invoker<T>> invokers = new ArrayList<>(providers.size());
        for (String provider : providers) {
            String s = URLDecoder.decode(provider);
            URL url = URL.buildFromAddress(s);
            String address = host2IpAddress(url.getHost());
            int port = url.getPort();
            // RemoteInvoker 为实际进行数据交互的 invoker
            RemoteInvoker invoker = new RemoteInvoker(address, port, client, provider, this.url);
            invokers.add(invoker);
        }
        return invokers;
    }

    /**
     * 将 host 转换为 ip address
     *
     * @param host 主机名
     * @return
     */
    private String host2IpAddress(String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IllegalStateException("Got an host " + host + " from registry center, but the host can't be converted " +
                    "to ip address");
        }
    }

    /**
     * invokers 刷新监听器
     */
    private class InvokersRefreshListener implements PathChildrenCacheListener {
        private final PathChildrenCache pathCache;

        public InvokersRefreshListener(PathChildrenCache pathCache) {
            this.pathCache = pathCache;
        }

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            PathChildrenCacheEvent.Type type = event.getType();
            switch (type) {
                case CHILD_ADDED:
                case CHILD_REMOVED:
                case CHILD_UPDATED: {
                    List<ChildData> currentData = pathCache.getCurrentData();
                    List<String> providerUrls = new ArrayList<>(currentData.size());
                    for (ChildData data : currentData) {
                        providerUrls.add(data.getPath());
                    }
                    // 这里是保存到 concurrent hash map 中，能够保证可见性
                    saveProvidersToBeehiveContext(url.getParameter(SERVICE), providerUrls);
                    FailoverClusterInvoker.this.needUpdateInvokers = true;
                }
            }
        }
    }

    /**
     * 保存到当前线程的 load balance
     */
    private class LoadBalanceThreadLocal extends ThreadLocal<LoadBalance> {
        // empty
    }

    /**
     * 保存到当前线程的 retry count
     */
    private class RetryCountThreadLocal extends ThreadLocal<Integer> {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    }

    /**
     * 用于将 service 对应的 providers 保存到 BeehiveContext 中
     *
     * @param service      服务接口名
     * @param providerUrls 注册中心上的 providers 集合
     */
    private void saveProvidersToBeehiveContext(String service, List<String> providerUrls) {
        Map providers = BeehiveContext.unsafeGet(PROVIDERS, Map.class);
        if (providers == null) {
            synchronized (BeehiveContext.providersMonitor) {
                providers = BeehiveContext.unsafeGet(PROVIDERS, Map.class);
                if (providers == null) {
                    providers = new HashMap<>();
                    //noinspection unchecked
                    providers.put(service, providerUrls);
                }
            }
        }
        //noinspection unchecked
        providers.put(service, providerUrls);
    }
}
