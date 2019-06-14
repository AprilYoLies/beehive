package top.aprilyolies.beehive.provider.service;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class Proxy0 extends top.aprilyolies.beehive.proxy.Proxy {
    public static String[] pns;
    public static java.util.Map pts;
    public static String[] mns;
    public static String[] dmns;
    public static Class[] mts0;

    public String[] Proxy() {
        return pns;
    }

    public boolean hasProperty(String n) {
        return pts.containsKey(n);
    }

    public Class getPropertyType(String n) {
        return (Class) pts.get(n);
    }

    public String[] getMethodNames() {
        return mns;
    }

    public String[] getDeclaredMethodNames() {
        return dmns;
    }

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
