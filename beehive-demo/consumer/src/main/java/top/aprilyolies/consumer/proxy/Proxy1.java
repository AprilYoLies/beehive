package top.aprilyolies.consumer.proxy;

/**
 * @Author EvaJohnson
 * @Date 2019-06-30
 * @Email g863821569@gmail.com
 */
public class Proxy1 implements top.aprilyolies.beehive.proxy.support.Proxy, top.aprilyolies.service.BeehiveService {
    public static java.lang.reflect.Method[] methods;
    private java.lang.reflect.InvocationHandler handler;

    public Proxy1(java.lang.reflect.InvocationHandler arg0) {
        handler = arg0;
    }

    public java.lang.String say(java.lang.String arg0) throws java.lang.Exception {
        Object[] args = new Object[1];
        args[0] = arg0;
        Object ret = null;
        try {
            ret = handler.invoke(this, methods[0], args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return (java.lang.String) ret;
    }

    public java.lang.String see(java.lang.String arg0) throws java.lang.Exception {
        Object[] args = new Object[1];
        args[0] = arg0;
        Object ret = null;
        try {
            ret = handler.invoke(this, methods[1], args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return (java.lang.String) ret;
    }
}
