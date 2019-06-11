package top.aprilyolies.provider;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class ServiceProvider {
    private static final String DEMO_SERVICE = "/META-INF/provider/demo-service";

    public static void main(String[] args) throws Exception {
        TestingServer server = new TestingServer();
        CuratorFramework provider = null;
        try {
            ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
            provider = CuratorFrameworkFactory.builder()
                    .connectString(server.getConnectString())
                    .retryPolicy(retryPolicy)
                    .connectionTimeoutMs(1000)
                    .sessionTimeoutMs(1000)
                    .build();
            provider.start();
            provider.create().creatingParentsIfNeeded().forPath(DEMO_SERVICE);
            List<String> services = provider.getChildren().forPath(DEMO_SERVICE.substring(0, DEMO_SERVICE.lastIndexOf("/")));
            System.out.println(services);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(server);
            CloseableUtils.closeQuietly(provider);
        }
    }
}
