package top.aprilyolies.beehive.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.common.result.Result;
import top.aprilyolies.beehive.filter.AccessLogFilter;
import top.aprilyolies.beehive.filter.Filter;
import top.aprilyolies.beehive.filter.MonitorFilter;
import top.aprilyolies.beehive.invoker.AbstractInvoker;
import top.aprilyolies.beehive.invoker.Invoker;
import top.aprilyolies.beehive.invoker.ProxyWrapperInvoker;
import top.aprilyolies.beehive.proxy.ProxyFactory;

import java.util.ArrayList;
import java.util.List;

import static top.aprilyolies.beehive.common.UrlConstants.*;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public class ZookeeperRegistry extends AbstractRegistry {
    private static final int CONNECT_TIMEOUT_TIME = 5000;

    private static final int RETRY_TIMES = 3;

    private static final int RETRY_INTERVAL = 1000;

    private CuratorFramework zkClient;

    private URL url;

    public ZookeeperRegistry(URL url) {
        this.url = url;
        String address = getRegistryAddress(url);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(address)
                .retryPolicy(new RetryNTimes(RETRY_TIMES, RETRY_INTERVAL))
                .connectionTimeoutMs(CONNECT_TIMEOUT_TIME);
        zkClient = builder.build();
        zkClient.start();
    }

    // 获取注册中心的地址
    private String getRegistryAddress(URL url) {
        return url.getHost() + ":" + url.getPort();
    }

    @Override
    protected void openServer(URL url) {
        String protocol = url.getOriginUrl().getParameterElseDefault(UrlConstants.SERVICE_PROTOCOL, UrlConstants.DEFAULT_SERVICE_PROTOCOL);
        URL serviceUrl = URL.copyFromUrl(url.getOriginUrl());
        serviceUrl.setOriginUrl(url.getOriginUrl());
        serviceUrl.setProtocol(protocol);
        if (url.isProvider()) {
            protocolSelector.publish(serviceUrl);
        } else if (!url.isProvider()) {
            protocolSelector.subscribe(serviceUrl);
        }
    }

    /**
     * 创建 invoker 并进行缓存
     *
     * @param url
     */
    @Override
    protected void createInvoker(URL url) {
        ProxyFactory proxyFactory = proxyFactorySelector.createProxyFactory(url);
        Invoker<?> invoker = proxyFactory.createProxy(url);
        Invoker<?> chain;
        if (url.isProvider()) {
            chain = buildInvokerChain(invoker);
            BeehiveContext.safePut(url.getParameter(SERVICE), chain);
        } else {
            String providerPath = getProviderPath(url);
            try {
                List<String> providerUrls = zkClient.getChildren().forPath(providerPath);
                BeehiveContext.safePut(PROVIDERS, providerUrls);
            } catch (Exception e) {
                logger.error("Can't get provider information at " + providerPath);
                throw new RuntimeException(e.getMessage(), e);
            }
            if (invoker instanceof ProxyWrapperInvoker) {
                ProxyWrapperInvoker proxyWrapperInvoker = (ProxyWrapperInvoker) invoker;
                BeehiveContext.safePut(url.getParameter(SERVICE), proxyWrapperInvoker.getProxy());
            }

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

    /**
     * 通过 filter 构建 invoker 链，最后一个 invoker 就是我们创建的 ProxyWrapperInvoker，它封装了我们真正的调用逻辑
     *
     * @param invoker 原始的 invoker
     * @return 通过 filter 构建出来的 invoker 链
     */
    private Invoker buildInvokerChain(Invoker<?> invoker) {
        // TODO 这里的 filter 获取应该通过 ExtensionLoader
        List<Filter> filters = new ArrayList<>();
        filters.add(new AccessLogFilter());
        filters.add(new MonitorFilter());
        Invoker ptr = invoker;
        if (filters.size() > 0) {
            for (Filter filter : filters) {
                final Invoker next = ptr;
                Invoker pre = new AbstractInvoker() {
                    @Override
                    protected Result doInvoke(InvokeInfo info) {
                        return filter.doFilter(next, info);
                    }
                };
                ptr = pre;
            }
        }
        //noinspection unchecked
        return ptr;
    }

    @Override
    protected void doPublish(URL url) throws Exception {
        try {
            String registryPath = getRegistryPath(url);
            createPath(registryPath, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归在注册中心创建路径信息
     *
     * @param registryPath 待创建的路径信息
     * @param ephemeral    创建的模式
     */
    private void createPath(String registryPath, boolean ephemeral) {
        if (isExisted(registryPath))
            return;
        int i = registryPath.lastIndexOf("/");
        if (i > 0) {
            String parent = registryPath.substring(0, i);
            createPath(parent, false);
        }
        if (ephemeral)
            createEphemeral(registryPath);
        else
            createPersistent(registryPath);
    }

    // 创建持久路径
    private void createPersistent(String registryPath) {
        try {
            zkClient.create().withMode(CreateMode.PERSISTENT).forPath(registryPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建非持久路径
    private void createEphemeral(String registryPath) {
        try {
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(registryPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查路径是否已经存在了
     *
     * @param registryPath
     * @return
     */
    private boolean isExisted(String registryPath) {
        try {
            return zkClient.checkExists().forPath(registryPath) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 构建的模式为 /group/serviceName/provider
    private String getRegistryPath(URL url) {
        if (url == null)
            throw new IllegalArgumentException("url should not be null");
        String group = url.getParameterElseDefault(GROUP_KEY, DEFAULT_GROUP);
        String serviceName = url.getPath();
        String category = url.getParameter(CATEGORY);
        String registorInfo;
        if (CONSUMERS.equals(category)) {
            registorInfo = url.getParameter(CONSUMER);
        } else {
            registorInfo = url.getParameter(PROVIDER);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(PATH_SEPARATOR)
                .append(group)
                .append(PATH_SEPARATOR)
                .append(serviceName)
                .append(PATH_SEPARATOR)
                .append(category)
                .append(PATH_SEPARATOR)
                .append(registorInfo);
        return sb.toString();
    }
}
