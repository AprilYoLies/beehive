package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.cluster.Cluster;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.invoker.Invoker;
import top.aprilyolies.beehive.proxy.support.ConsumerProxy;
import top.aprilyolies.beehive.proxy.support.ProviderProxy;
import top.aprilyolies.beehive.proxy.support.Proxy;
import top.aprilyolies.beehive.spring.ServiceConfigBean;

/**
 * @Author EvaJohnson
 * @Date 2019-06-30
 * @Email g863821569@gmail.com
 */
public class JdkProxyFactory extends AbstractProxyFactory {
    @Override
    protected Proxy createProxy(Class<?> clazz, URL url) {
        if (url.isProvider()) {
            // 根据 url 信息获取 invoke target 实例
            ServiceConfigBean serviceConfigBean = BeehiveContext.unsafeGet(UrlConstants.PROVIDER_MODEL, ServiceConfigBean.class);
            Object target = serviceConfigBean.getRef();
            return ProviderProxy.getJdkProxy(target, clazz, Proxy.class);
        } else {
            Invoker invoker = getInvoker(url);
            return ConsumerProxy.getJdkProxy(new InvokerInvocationHandler(invoker, url), clazz, Proxy.class);
        }
    }

    @Override
    public ProxyFactory createProxyFactory(URL url) {
        throw new UnsupportedOperationException("Only ProxyFactorySelector could call this method");
    }

    private Invoker getInvoker(URL url) {
        Cluster selector = ExtensionLoader.getExtensionLoader(Cluster.class).getExtensionSelectorInstance();
        return selector.join(url);
    }
}
