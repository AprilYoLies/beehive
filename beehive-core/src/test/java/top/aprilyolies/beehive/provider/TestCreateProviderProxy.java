package top.aprilyolies.beehive.provider;

import org.junit.Test;
import top.aprilyolies.beehive.provider.service.DemoService;
import top.aprilyolies.beehive.provider.service.DemoServiceImpl;
import top.aprilyolies.beehive.proxy.support.ProviderProxy;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class TestCreateProviderProxy {
    @Test
    public void testCreatProxy() {
        ProviderProxy providerProxy = ProviderProxy.getProxy(DemoService.class);
    }

    @Test
    public void testProxyInvoke() throws NoSuchMethodException, InvocationTargetException {
        ProviderProxy providerProxy = ProviderProxy.getProxy(DemoService.class);
        DemoServiceImpl demoService = new DemoServiceImpl();
        String methodName = "say";
        Class<?>[] pts = new Class[]{String.class};
        Object[] pvs = new Object[]{"beehive"};
        providerProxy.invokeMethod(demoService, methodName, pts, pvs);
    }
}
