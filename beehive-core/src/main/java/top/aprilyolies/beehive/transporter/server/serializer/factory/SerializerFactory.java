package top.aprilyolies.beehive.transporter.server.serializer.factory;

import io.netty.buffer.ByteBuf;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.transporter.server.serializer.Serializer;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */

/**
 * 此接口的实现类代表着序列化器工厂
 */
@SPI("hessian")
public interface SerializerFactory {
    /**
     * 获取对应的序列化工厂
     *
     * @param url 优先获取 url 的 SERIALIZER 参数对应的 serializer，默认为 hessian
     * @param buf 序列化的
     * @return
     */
    @Selector
    Serializer serializer(URL url, ByteBuf buf);

    @Selector
    Serializer deserializer(URL url, ByteBuf buf);
}
