package top.aprilyolies.consumer;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import top.aprilyolies.service.UserService;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class UserConsumer {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        UserService userService = context.getBean("userService", UserService.class);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 400000; i++) {
            String result = userService.findUserById(i);
            System.out.println("result: " + result);
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
