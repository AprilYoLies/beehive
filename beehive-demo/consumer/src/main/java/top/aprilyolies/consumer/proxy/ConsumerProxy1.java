package top.aprilyolies.consumer.proxy;

/**
 * @Author EvaJohnson
 * @Date 2019-06-30
 * @Email g863821569@gmail.com
 */
public class ConsumerProxy1 extends top.aprilyolies.beehive.proxy.support.ConsumerProxy {
    public Object newInstance(java.lang.reflect.InvocationHandler h) {
        return new Proxy1(h);
    }
}
