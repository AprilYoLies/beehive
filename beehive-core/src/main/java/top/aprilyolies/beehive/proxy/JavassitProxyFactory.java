package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.proxy.support.ConsumerProxy;
import top.aprilyolies.beehive.proxy.support.ProviderProxy;
import top.aprilyolies.beehive.proxy.support.Proxy;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class JavassitProxyFactory extends AbstractProxyFactory {

    @Override
    public ProxyFactory createProxyFactory(URL url) {
        throw new UnsupportedOperationException("Only ProxyFactorySelector could call this method");
    }

    @Override
    protected Proxy createProxy(Class<?> clazz, URL url) {
        if (url.isProvider())
            return ProviderProxy.getProxy(clazz);
        else
            return (Proxy) ConsumerProxy.getProxy(clazz, Proxy.class).newInstance((proxy, method, args) -> args[0] + " world");
    }
}
