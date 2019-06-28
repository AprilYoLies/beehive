package top.aprilyolies.consumer;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import top.aprilyolies.service.BeehiveService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class MultiThreadConsumer {
    // FIXME 当这里的线程较多时，比如 10，RemoteInvoker 中的 thread local 逻辑有错误
    // 拟测试的线程数
    private static int THREADS = 5;

    public static void main(String[] args) throws Exception {
        // 计数栅栏
        CountDownLatch latch = new CountDownLatch(THREADS);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS, new DemoThreadFactory());
        // 多个线程进行访问
        for (int i = 0; i < THREADS; i++) {
            executor.submit(() -> {
                try {
                    for (int i1 = 0; i1 < 2000; i1++) {
                        BeehiveService service = context.getBean("demoService", BeehiveService.class);
                        String hello = service.say(Thread.currentThread().getName() + " - " + i1);
                        System.out.println("result: " + hello);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        long start = System.currentTimeMillis();
        latch.await();
        System.out.println(System.currentTimeMillis() - start);
    }

    private static class DemoThreadFactory implements ThreadFactory {
        AtomicLong id = new AtomicLong(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MultiThreadConsumer-pool-thread-" + id.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
