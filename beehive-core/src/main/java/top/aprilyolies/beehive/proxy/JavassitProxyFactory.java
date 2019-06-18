package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.proxy.support.ProviderProxy;

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
    protected ProviderProxy createProxy(Class<?> clazz) {
        return ProviderProxy.getProxy(clazz);
    }
}
