package top.aprilyolies.beehive.protocol;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.extension.annotation.Selector;

/**
 * beehive 程序内部的逻辑走向的向导，程序会根据 url 的协议类型来决定接下来该进行什么操作
 */
@SPI
public interface Protocol {
    /**
     * 发布服务的函数逻辑，依赖于 url 的协议类型
     *
     * @param url
     */
    @Selector
    void publish(URL url);

    @Selector
    void subscribe(URL url);
}
