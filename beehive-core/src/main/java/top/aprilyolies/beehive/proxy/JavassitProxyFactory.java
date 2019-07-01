package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.cluster.Cluster;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.invoker.Invoker;
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
    protected Proxy createProxy(Class<?> clazz, URL url) {
        if (url.isProvider())
            return ProviderProxy.getProxy(clazz);
        else {
            Invoker invoker = getInvoker(url);
            return (Proxy) ConsumerProxy.
                    getProxy(clazz, Proxy.class).
                    newInstance(new InvokerInvocationHandler(invoker, url));
        }

    }

    private Invoker getInvoker(URL url) {
        Cluster selector = ExtensionLoader.getExtensionLoader(Cluster.class).getExtensionSelectorInstance();
        return selector.join(url);
    }
}
