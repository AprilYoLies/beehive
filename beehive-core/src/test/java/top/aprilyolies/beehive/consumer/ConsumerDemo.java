package top.aprilyolies.beehive.consumer;

import top.aprilyolies.beehive.provider.ServiceConsumer;
import top.aprilyolies.beehive.provider.service.DemoService;
import top.aprilyolies.beehive.spring.RegistryConfigBean;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class ConsumerDemo {
    public static void main(String[] args) throws Exception {
        ServiceConsumer consumer = new ServiceConsumer();
        RegistryConfigBean registry = new RegistryConfigBean();
        consumer.setProtocol("beehive");
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        consumer.setRegistry(registry);
        consumer.setService(DemoService.class.getName());
        Object obj = consumer.getObject();
        DemoService demoService = (DemoService) obj;
        long start = System.currentTimeMillis();
        for (int i = 1; i <= 10000; i++) {
            String res = demoService.say("hello - " + i);
            System.out.println(res);
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
