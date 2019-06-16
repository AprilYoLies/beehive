package top.aprilyolies.beehive.transporter.server.serializer;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.extension.annotation.Selector;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */

/**
 * 此接口的实现类代表着序列化器，
 */
@SPI("hessian")
public interface Serializer {
    @Selector
    Serializer getSerializer(URL url);
}
