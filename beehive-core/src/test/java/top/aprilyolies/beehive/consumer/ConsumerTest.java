package top.aprilyolies.beehive.consumer;

import org.junit.Test;
import top.aprilyolies.beehive.provider.ServiceConsumer;
import top.aprilyolies.beehive.provider.service.DemoService;
import top.aprilyolies.beehive.spring.RegistryConfigBean;

import java.util.Scanner;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class ConsumerTest {
    @Test
    public void createConfigBean() throws Exception {
        ServiceConsumer consumer = new ServiceConsumer();
        RegistryConfigBean registry = new RegistryConfigBean();
        consumer.setProtocol("beehive");
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        consumer.setRegistry(registry);
        consumer.setService(DemoService.class.getName());
        Object obj = consumer.getObject();
        DemoService demoService = (DemoService) obj;
        String res = demoService.say("hello");
        new Scanner(System.in).nextLine();
    }
}
