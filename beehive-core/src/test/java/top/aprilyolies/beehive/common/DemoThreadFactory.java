package top.aprilyolies.beehive.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author EvaJohnson
 * @Date 2019-06-28
 * @Email g863821569@gmail.com
 */
public class DemoThreadFactory implements ThreadFactory {
    AtomicLong id = new AtomicLong(1);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("MultiThreadConsumer-pool-thread-" + id.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
