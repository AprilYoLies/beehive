package top.aprilyolies.beehive.invoker;

import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.proxy.support.ConsumerProxy;
import top.aprilyolies.beehive.proxy.support.ProviderProxy;
import top.aprilyolies.beehive.proxy.support.Proxy;

import java.lang.reflect.Method;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class ProxyWrapperInvoker<T> extends AbstractInvoker<T> {
    // 发布的服务的代理类
    private final Proxy proxy;
    // 发布的服务的类型
    private final Class<T> type;
    private final URL url;

    public Proxy getProxy() {
        return proxy;
    }

    public Class<T> getType() {
        return type;
    }

    public URL getUrl() {
        return url;
    }

    public ProxyWrapperInvoker(Proxy proxy, Class<T> type, Object target, URL url) {
        this.proxy = proxy;
        this.type = type;
        this.url = url;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) {
        String methodName = info.getMethodName();
        Class<?>[] pts = info.getPts();
        Object[] pvs = info.getPvs();
        Object target = info.getTarget();
        if (url.isProvider()) {
            if (isJdkProxy(url)) {
                try {
                    Method method = proxy.getClass().getMethod(methodName, pts);
                    return method.invoke(proxy, pvs);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                ProviderProxy providerProxy = (ProviderProxy) proxy;
                try {
                    return providerProxy.invokeMethod(target, methodName, pts, pvs);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

        } else {
            ConsumerProxy consumerProxy = (ConsumerProxy) proxy;
            return consumerProxy.newInstance();
        }
    }

    /**
     * 判断当期是否是采用的 jdk 动态代理方式
     *
     * @param url
     * @return
     */
    private boolean isJdkProxy(URL url) {
        return url.getParameter(UrlConstants.PROXY_FACTORY).equals("jdk");
    }
}
