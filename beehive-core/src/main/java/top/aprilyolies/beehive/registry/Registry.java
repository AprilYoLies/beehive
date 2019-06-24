package top.aprilyolies.beehive.registry;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */

/**
 * 服务注册接口，不同的实现类实现不同注册中心的注册
 */
@SPI
public interface Registry {
    // 用于向注册中心进行方法的注册
    void registry(URL url);

    // 用于关闭注册中心
    void close();
}
