package top.aprilyolies.beehive.provider.service;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public class ConsumerProxy0 implements top.aprilyolies.beehive.provider.service.DemoService {
    public static java.lang.reflect.Method[] methods;
    private java.lang.reflect.InvocationHandler handler;

    public ConsumerProxy0(java.lang.reflect.InvocationHandler arg0) {
        handler = arg0;
    }

    public java.lang.String say(java.lang.String arg0) {
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
}
