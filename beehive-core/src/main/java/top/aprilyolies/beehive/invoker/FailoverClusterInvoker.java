package top.aprilyolies.beehive.invoker;

import org.apache.curator.framework.CuratorFramework;
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
import java.util.List;
import java.util.Map;

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
    private static Registry regsitry;
    // 最大重复 invoke 次数
    private final int MAX_REINVOKE_TIMES = 5;
    // 记录当前重复 invoke 的次数
    private final ThreadLocal<Integer> retryCountThreadLocal = new RetryCountThreadLocal();
    // 更新 invokers 的标志，在有服务上线或者下线后，此标志会设置为 true
    private volatile boolean needUpdateInvokers = false;
    // 用于缓存当前的 providers，invokers 将会根据此 providers 来构建
    private List<String> providers;
    // 保存到当前线程中的 load balance
    private final ThreadLocal<LoadBalance> loadBalanceThreadLocal = new LoadBalanceThreadLocal();

    public FailoverClusterInvoker(URL url) {
        this.url = url;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) {
        try {
            if (this.invokers == null) {
                invokers = listInvokers();
            }
            if (needUpdateInvokers) {
                needUpdateInvokers = false;
                updateInvokers();
            }
            if (regsitry == null) {
                regsitry = BeehiveContext.unsafeGet(UrlConstants.REGISTRIES, Registry.class);
                addInvokersRefreshListener(regsitry);
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
                if (reInvokeCount++ < MAX_REINVOKE_TIMES) {
                    retryCountThreadLocal.set(reInvokeCount);
                    return doInvoke(info);
                } else {
                    throw new IllegalStateException("There is none of invoker could be used");
                }
            }
        } finally {
            retryCountThreadLocal.set(0);
        }
    }

    /**
     * 更新 invokers 信息
     */
    @SuppressWarnings("unchecked")
    private void updateInvokers() {
        List<Invoker<T>> oldInvokers = this.invokers;
        List<Invoker<T>> newInvokers = new ArrayList<>();
        List<String> oldProviders = this.providers;
        @SuppressWarnings("unchecked") List<String> newProviders = BeehiveContext.unsafeGet(UrlConstants.PROVIDERS, List.class);
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
    }

    /**
     * 根据指定的 providers 构建 invokers
     *
     * @param providers
     * @return
     */
    private List<Invoker<T>> buildInvokers(List<String> providers) {
        @SuppressWarnings("unchecked") Map<String, Client> clientCache = BeehiveContext.unsafeGet(UrlConstants.CONSUMERS_TRANSPORT, Map.class);
        Client server = clientCache.get(url.getParameter(UrlConstants.SERVICE));
        assert providers != null;
        return createRemoteInvoker(providers, server);
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
                String registryPath = zkRegistry.getRegistryPath();
                PathChildrenCache pathCache = new PathChildrenCache(client, registryPath, true);
                pathCache.start();
                pathCache.getListenable().addListener(new InvokersRefreshListener());
            }
        } catch (Exception e) {
            logger.error("Can't add invokers refresh listener");
            e.printStackTrace();
        }
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

    private List<Invoker<T>> listInvokers() {
        //noinspection unchecked
        List<String> providers = BeehiveContext.unsafeGet(UrlConstants.PROVIDERS, List.class);
        this.providers = providers;
        @SuppressWarnings("unchecked") Map<String, Client> clientCache = BeehiveContext.unsafeGet(UrlConstants.CONSUMERS_TRANSPORT, Map.class);
        Client server = clientCache.get(url.getParameter(UrlConstants.SERVICE));
        assert providers != null;
        return createRemoteInvoker(providers, server);
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
            RemoteInvoker invoker = new RemoteInvoker(address, port, client, provider);
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
        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            PathChildrenCacheEvent.Type type = event.getType();
            switch (type) {
                case CHILD_ADDED:
                case CHILD_REMOVED:
                case CHILD_UPDATED: {
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
}
