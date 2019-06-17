package top.aprilyolies.beehive.transporter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author EvaJohnson
 * @Date 2019-06-17
 * @Email g863821569@gmail.com
 */
public class BeehiveThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_ID = new AtomicInteger(1);

    private final AtomicInteger THREAD_ID = new AtomicInteger(1);

    // prefix-thread-
    private final String prefix;

    private final boolean daemon;

    private final ThreadGroup group;

    public BeehiveThreadFactory() {
        this("pool-" + POOL_ID.getAndIncrement(), false);
    }

    public BeehiveThreadFactory(String prefix) {
        this(prefix, false);
    }

    public BeehiveThreadFactory(String prefix, boolean daemon) {
        // pool-1-thread-
        this.prefix = prefix + "-thread-";
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    // 创建普通线程
    @Override
    public Thread newThread(Runnable runnable) {
        // pool-1-thread-1
        String name = prefix + THREAD_ID.getAndIncrement();
        // 这里创建的是普通线程
        Thread ret = new Thread(group, runnable, name, 0);
        ret.setDaemon(daemon);
        return ret;
    }

    public ThreadGroup getThreadGroup() {
        return group;
    }
}
