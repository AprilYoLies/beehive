package top.aprilyolies.consumer;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import top.aprilyolies.service.BeehiveService;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class Consumer {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        BeehiveService demoService = context.getBean("demoService", BeehiveService.class);
        for (int i = 0; i < 50000; i++) {
            String hello = demoService.say("world - " + i);
            System.out.println("result: " + hello);
            Thread.sleep(500);
        }
    }
}
