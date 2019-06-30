package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.invoker.Invoker;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */

/**
 * 该接口的实现类需要根据 URL 的 ref 参数来构建代理类，返回结果通过 Invoker 实现类进行封装
 */
@SPI("jdk")
public interface ProxyFactory {
    /**
     * 该方法主要是获取 url 的 ref 参数，构建代理类，然后将其封装成为 Invoker 返回
     *
     * @param url 需要验证 url 的 ref 参数是否为空或者空串
     * @return 封装个代理类
     */
    <T> Invoker<T> createProxy(URL url);

    /**
     * 获取 ProxyFactory 实例，只有 ProxyFactorySelector 应该实现该方法，其他实例应该抛出 UnsupportedOperationException 异常
     *
     * @return
     */
    ProxyFactory createProxyFactory(URL url);
}
