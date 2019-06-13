package top.aprilyolies.beehive.protocol;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public class ZookeeperRegistry extends AbstractRegistryProtocol {
    private static final int CONNECT_TIMEOUT_TIME = 5000;

    private static final int RETRY_TIMES = 3;

    private static final int RETRY_INTERVAL = 1000;

    private final CuratorFramework zkClient;

    private final URL url;

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

    private String getRegistryAddress(URL url) {
        return url.getHost() + ":" + url.getPort();
    }

    @Override
    protected void doPublish(URL url) throws Exception {
        String registryPath = getRegistryPath(url);
        zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(registryPath);
    }

    // 构建的模式为 /group/serviceName/provider
    private String getRegistryPath(URL url) {
        if (url == null)
            throw new IllegalArgumentException("url should not be null");
        String group = url.getParameter(UrlConstants.GROUP_KEY);
        String serviceName = url.getPath();
        String provider = url.getParameter(UrlConstants.PROVIDER);
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(group).append("/").append(serviceName).append("/").append(provider);
        return sb.toString();
    }
}
