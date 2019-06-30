package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.proxy.support.ProviderProxy;
import top.aprilyolies.beehive.proxy.support.Proxy;

/**
 * @Author EvaJohnson
 * @Date 2019-06-30
 * @Email g863821569@gmail.com
 */
public class JdkProxyFactory extends AbstractProxyFactory {
    @Override
    protected Proxy createProxy(Class<?> clazz, URL url) {
        if (url.isProvider())
            return ProviderProxy.getJdkProxy(clazz, Proxy.class);
        else {
//            Invoker invoker = getInvoker(url);
//            return (Proxy) ConsumerProxy.
//                    getProxy(clazz, Proxy.class).
//                    newInstance(new InvokerInvocationHandler(invoker, url));
            return null;
        }
    }

    @Override
    public ProxyFactory createProxyFactory(URL url) {
        throw new UnsupportedOperationException("Only ProxyFactorySelector could call this method");
    }
}
