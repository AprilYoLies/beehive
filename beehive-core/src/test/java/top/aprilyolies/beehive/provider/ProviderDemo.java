package top.aprilyolies.beehive.provider;

import top.aprilyolies.beehive.provider.service.DemoService;
import top.aprilyolies.beehive.provider.service.DemoServiceImpl;
import top.aprilyolies.beehive.spring.RegistryConfigBean;

import java.util.Scanner;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class ProviderDemo {
    public static void main(String[] args) {
        ServiceProvider provider = new ServiceProvider();
        RegistryConfigBean registry = new RegistryConfigBean();
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        provider.setRegistry(registry);
        DemoServiceImpl demoService = new DemoServiceImpl();
        provider.setRef(demoService);
        provider.setService(DemoService.class.getName());
        provider.setProtocol("beehive");
        provider.exportService();
        new Scanner(System.in).nextLine();
    }
}
