package top.aprilyolies.curator.counter;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author EvaJohnson
 * @Date 2019-06-05
 * @Email g863821569@gmail.com
 */
public class SharedCounterExample implements SharedCountListener {

    private static final int QTY = 5;
    private static final String PATH = "/examples/counter";

    public static void main(String[] args) throws IOException, Exception {
        final Random rand = new Random();
        SharedCounterExample example = new SharedCounterExample();
        CuratorFramework client = null;
        try (TestingServer server = new TestingServer()) {
            client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start();

            SharedCount baseCount = new SharedCount(client, PATH, 0);
            baseCount.addListener(example);
            baseCount.start();

            List<SharedCount> examples = Lists.newArrayList();
            ExecutorService service = Executors.newFixedThreadPool(QTY);
            for (int i = 0; i < QTY; ++i) {
                final SharedCount count = new SharedCount(client, PATH, 0);
                examples.add(count);
                Callable<Void> task = () -> {
                    count.start();
                    Thread.sleep(2000);
                    while (!count.trySetCount(count.getVersionedValue(), count.getCount() + 5)) ;
                    System.out.println("Increment:" + true);
                    return null;
                };
                service.submit(task);
            }

            service.shutdown();
            service.awaitTermination(15, TimeUnit.SECONDS);

            for (int i = 0; i < QTY; ++i) {
                examples.get(i).close();
            }
            baseCount.close();
        } finally {
            client.close();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    @Override
    public void stateChanged(CuratorFramework arg0, ConnectionState arg1) {
        System.out.println("State changed: " + arg1.toString());
    }

    @Override
    public void countHasChanged(SharedCountReader sharedCount, int newCount) throws Exception {
        System.out.println("Counter's value is changed to " + newCount);
    }
}