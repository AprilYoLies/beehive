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
 * TODO
 */
@SPI
public interface Protocol {
    @Selector
    void publish(URL url);
}
