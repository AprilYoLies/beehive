package top.aprilyolies.beehive.common;

import org.junit.Test;
import top.aprilyolies.beehive.spring.AbstractConfig;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class CommonTest {
    @Test
    public void testGetAddress() {
        try {
            System.out.println(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetBeanName() {
        System.out.println(StringUtils.getBeanName(ServiceConfigBean.class));
    }

    @Test
    public void testBeehiveShutdownHook() {
        System.out.println(AbstractConfig.BeehiveShutdownHook.BEEHIVE_SHUTDOWN_HOOK);
        new Thread(() -> {
            System.out.println(AbstractConfig.BeehiveShutdownHook.BEEHIVE_SHUTDOWN_HOOK);
        }).start();
    }

    @Test
    public void testRandom() {
        Random random = new Random(7440);
        for (int i = 0; i < 100; i++) {
            System.out.println(Math.abs(random.nextInt() % 2));
        }
    }

    @Test
    public void testHost2IpAddress() {
        String host = "192.168.1.1";
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            System.out.println(inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IllegalStateException("Got an host " + host + " from registry center, but the host can't be converted " +
                    "to ip address");
        }
    }

    @Test
    public void testSynchronizedQueue() throws InterruptedException {
        BlockingQueue<Integer> queue = new SynchronousQueue<>();
        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    System.out.println(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(20);
        System.out.println(queue.offer(1) + " ");
        Thread.sleep(20);
        System.out.println(queue.offer(2) + " ");
        Thread.sleep(20);
        System.out.println(queue.offer(3) + " ");
    }
}

