package top.aprilyolies.beehive.provider;

import org.junit.Test;
import top.aprilyolies.beehive.provider.service.DemoService;
import top.aprilyolies.beehive.provider.service.DemoServiceImpl;
import top.aprilyolies.beehive.spring.RegistryConfigBean;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class ProviderTest {
    @Test
    public void createConfigBean() {
        ServiceProvider provider = new ServiceProvider();
        RegistryConfigBean registry = new RegistryConfigBean();
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        provider.setRegistry(registry);
        provider.setRef(DemoServiceImpl.class.getName());
        provider.setService(DemoService.class.getName());
        provider.exportService();
    }
}
