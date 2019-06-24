package top.aprilyolies.beehive.common;

import top.aprilyolies.beehive.transporter.BeehiveThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author EvaJohnson
 * @Date 2019-06-24
 * @Email g863821569@gmail.com
 */
public class ExecutorTest {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool(new BeehiveThreadFactory("beehive-thread-", true));
        pool.submit((Runnable) () -> {
            while (true) {
                ;
            }
        });
    }
}
