package top.aprilyolies.beehive.common;

import top.aprilyolies.beehive.transporter.BeehiveThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author EvaJohnson
 * @Date 2019-06-25
 * @Email g863821569@gmail.com
 */
public class DiscardPolicyTest {
    public static void main(String[] args) {
        int FINAL_CHANNEL_HANDLER_THREADS = 3;
        String DEFAULT_THREAD_NAME = "test-discard-policy-thread";
        final ExecutorService executor = new ThreadPoolExecutor(FINAL_CHANNEL_HANDLER_THREADS,
                FINAL_CHANNEL_HANDLER_THREADS, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(3),
                new BeehiveThreadFactory(DEFAULT_THREAD_NAME, false),
                new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 100; i++) {
            Runnable runnable = () -> {
                while (true) {
                    System.out.println(Thread.currentThread().getName());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            System.out.println(i);
            executor.submit(runnable);
        }
    }
}
