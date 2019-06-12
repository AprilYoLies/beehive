package top.aprilyolies.beehive.injector;

import top.aprilyolies.beehive.extension.annotation.SPI;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */

/**
 * 属性注入器，用于从不同的环境获取名字为 name，类型为 type 的 bean，环境可以是 spring 容器，也可以是自行实现的 ioc 容器
 */
@SPI
public interface PropertyInjector {
    /**
     * 根据 type 和 name 从不同的环境获取相应的 bean
     *
     * @param type 要获取的 bean 的类型
     * @param name 在容器中，代表 bean 的名字
     * @param <T>
     * @return 根据 type 和 name 从容器中获取的 bean
     */
    <T> T inject(Class<T> type, String name);
}
