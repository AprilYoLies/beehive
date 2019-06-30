package top.aprilyolies.beehive.proxy;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.invoker.Invoker;
import top.aprilyolies.beehive.invoker.ProxyWrapperInvoker;
import top.aprilyolies.beehive.proxy.support.Proxy;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.ClassUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public abstract class AbstractProxyFactory implements ProxyFactory {
    protected final Logger logger = Logger.getLogger(getClass());

    @Override
    public <T> Invoker<T> createProxy(URL url) {
        if (logger.isDebugEnabled())
            logger.debug("Create proxy for the given url " + url);
        if (url == null) {
            throw new RuntimeException("Can't create proxy invoker for the given url");
        }
        try {
            String service = url.getParameter(UrlConstants.SERVICE);
            @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) ClassUtils.forName(service);
            Object target = null;
            if (url.isProvider()) {
                ServiceConfigBean serviceConfigBean = BeehiveContext.unsafeGet(UrlConstants.PROVIDER_MODEL, ServiceConfigBean.class);
                assert serviceConfigBean != null;
                target = serviceConfigBean.getRef();
            }
            return createInvoker(clazz, target, url);
        } catch (Exception e) {
            throw new IllegalStateException("Can't create invoker", e.getCause());
        }
    }

    private <T> Invoker<T> createInvoker(Class<T> clazz, Object target, URL url) {
        Proxy proxy = createProxy(clazz, url);
        //noinspection unchecked
        return new ProxyWrapperInvoker<T>(proxy, clazz, target, url);
    }

    protected abstract Proxy createProxy(Class<?> clazz, URL url);
}
