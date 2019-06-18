package top.aprilyolies.beehive.invoker;

import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.proxy.support.ProviderProxy;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class ProxyWrapperInvoker<T> extends AbstractInvoker<T> {
    // 发布的服务的代理类
    private final ProviderProxy proxy;
    // 发布的服务的类型
    private final Class<T> type;

    public ProxyWrapperInvoker(ProviderProxy proxy, Class<T> type, Object target) {
        this.proxy = proxy;
        this.type = type;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) throws NoSuchMethodException, InvocationTargetException {
        String methodName = info.getMethodName();
        Class<?>[] pts = info.getPts();
        Object[] pvs = info.getPvs();
        Object target = info.getTarget();
        return proxy.invokeMethod(target, methodName, pts, pvs);
    }
}
