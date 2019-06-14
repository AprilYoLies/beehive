package top.aprilyolies.beehive.registry.factory;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.registry.Registry;

/**
 * 该接口的实现类创建各自对应的 Registry 实现类
 */
@SPI
public interface RegistryFactory {
    // 实现类需要通过此方法，根据 url 构建对应的 Registry 实现类
    @Selector
    Registry createRegistry(URL url);
}
