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

    public FailoverClusterInvoker(URL url) {
        this.url = url;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) {
        if (this.invokers == null) {
            invokers = listInvokers();
        }
        if (regsitry == null) {
            regsitry = BeehiveContext.unsafeGet(UrlConstants.REGISTRIES, Registry.class);
            addInvokersRefreshListener(regsitry);
        }
        LoadBalance loadBalance = createLoadBalance(url);
        Invoker<T> invoker = selectInvoker(loadBalance, invokers);
        if (invoker != null) {
            Invoker chain = buildInvokerChain(invoker);
            Object result = chain.invoke(info);
            if (result == null) {
                return doInvoke(info);
            } else return result;
        } else {
            return doInvoke(info);
        }
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
            RemoteInvoker invoker = new RemoteInvoker(address, port, client);
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
                    FailoverClusterInvoker.this.invokers = null;
                }
            }
        }
    }
}
