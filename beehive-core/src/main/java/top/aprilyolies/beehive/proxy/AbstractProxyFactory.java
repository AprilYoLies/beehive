package top.aprilyolies.beehive.proxy;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.invoker.Invoker;
import top.aprilyolies.beehive.proxy.support.Proxy;
import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.StringUtils;

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
        if (url == null || StringUtils.isEmpty(url.getParameter(UrlConstants.SERVICE_REF))) {
            throw new RuntimeException("Can't create proxy invoker for the given url");
        }
        String ref = url.getParameter(UrlConstants.SERVICE_REF);
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) ClassUtils.forName(ref);
        return createInvoker(clazz);
    }

    private <T> Invoker<T> createInvoker(Class<T> clazz) {
        Proxy proxy = createProxy(clazz);
        return null;
    }

    protected abstract Proxy createProxy(Class<?> clazz);
}
