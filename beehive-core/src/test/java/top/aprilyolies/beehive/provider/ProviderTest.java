package top.aprilyolies.beehive.provider;

import org.junit.Assert;
import org.junit.Test;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.invoker.Invoker;
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

    @Test
    public void testCreateInovkeChain() {
        ServiceProvider provider = new ServiceProvider();
        RegistryConfigBean registry = new RegistryConfigBean();
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        provider.setRegistry(registry);
        provider.setRef(DemoServiceImpl.class.getName());
        provider.setService(DemoService.class.getName());
        provider.exportService();
    }

    @Test
    public void testCallInvokerChain() {
        ServiceProvider provider = new ServiceProvider();
        RegistryConfigBean registry = new RegistryConfigBean();
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        provider.setRegistry(registry);
        provider.setRef(DemoServiceImpl.class.getName());
        provider.setService(DemoService.class.getName());
        provider.exportService();

        DemoServiceImpl target = new DemoServiceImpl();
        String methodName = "say";
        Class<?>[] pts = new Class[]{String.class};
        Object[] pvs = new Object[]{"beehive"};
        String serviceName = "top.aprilyolies.beehive.provider.service.DemoService";
        InvokeInfo invokeInfo = new InvokeInfo(methodName, pts, pvs, target, serviceName);

        Invoker chain = BeehiveContext.safeGet(serviceName, Invoker.class);
        chain.invoke(invokeInfo);
    }

    @Test
    public void testCacheInvokerChain() {
        ServiceProvider provider = new ServiceProvider();
        RegistryConfigBean registry = new RegistryConfigBean();
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        provider.setRegistry(registry);
        provider.setRef(DemoServiceImpl.class.getName());
        provider.setService(DemoService.class.getName());
        provider.exportService();

        final Invoker chain = BeehiveContext.safeGet(provider.getService(), Invoker.class);

        new Thread(() -> {
            Invoker chain1 = BeehiveContext.safeGet(provider.getService(), Invoker.class);
            Assert.assertNotSame(chain, chain1);
        }).start();

    }

    @Test
    public void testStartServer() {
        ServiceProvider provider = new ServiceProvider();
        RegistryConfigBean registry = new RegistryConfigBean();
        registry.setAddress(new String[]{"zookeeper://127.0.0.1:2181"});
        provider.setRegistry(registry);
        provider.setRef(DemoServiceImpl.class.getName());
        provider.setService(DemoService.class.getName());
        provider.exportService();
    }
}
