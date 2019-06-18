package top.aprilyolies.beehive.provider.service;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */

import top.aprilyolies.beehive.proxy.support.ProviderProxy;

/**
 * 根据 DemoService 构建出来的代理类的代码
 */
public class ProviderProxy0 extends ProviderProxy {
    public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws java.lang.reflect.InvocationTargetException, java.lang.NoSuchMethodException {
        top.aprilyolies.beehive.provider.service.DemoService w;
        try {
            w = ((top.aprilyolies.beehive.provider.service.DemoService) o);
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
        try {
            if ("say".equals(n) && p.length == 1) {
                return w.say((java.lang.String) v[0]);
            }
        } catch (Throwable e) {
            throw new java.lang.reflect.InvocationTargetException(e);
        }
        throw new java.lang.NoSuchMethodException("Not found method \"" + n + "\" in class top.aprilyolies.beehive.provider.service.DemoService.");
    }
}
