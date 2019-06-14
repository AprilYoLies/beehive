package top.aprilyolies.beehive.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;

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
    protected void doPublish(URL url) throws Exception {
        try {
            String registryPath = getRegistryPath(url);
            createPath(registryPath, false);
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
        String provider = url.getParameter(UrlConstants.PROVIDER);
        String category = url.getParameter(CATEGORY);
        StringBuilder sb = new StringBuilder();
        sb.append(PATH_SEPARATOR)
                .append(group)
                .append(PATH_SEPARATOR)
                .append(serviceName)
                .append(PATH_SEPARATOR)
                .append(category)
                .append(PATH_SEPARATOR)
                .append(provider);
        return sb.toString();
    }
}
