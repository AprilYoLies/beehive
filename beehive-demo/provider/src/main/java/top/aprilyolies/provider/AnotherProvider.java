package top.aprilyolies.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @Author EvaJohnson
 * @Date 2019-07-03
 * @Email g863821569@gmail.com
 */
public class AnotherProvider {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("another-provider.xml");
        context.start();
        System.out.println("Provider started on thread " + Thread.currentThread().getName() + "..");
        System.in.read();
    }
}
