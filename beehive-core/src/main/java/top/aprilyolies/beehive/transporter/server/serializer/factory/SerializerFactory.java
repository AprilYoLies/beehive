package top.aprilyolies.beehive.transporter.server.serializer.factory;

import io.netty.buffer.ByteBuf;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */

/**
 * 此接口的实现类代表着序列化器工厂
 */
@SPI("fastjson")
public interface SerializerFactory {
    /**
     * 获取对应的序列化工厂
     *
     * @param url 优先获取 url 的 SERIALIZER 参数对应的 serializer，默认为 hessian
     * @param buf 序列化的结果将会存入其中
     * @return 真正的序列化器
     */
    OutputSerializer serializer(URL url, ByteBuf buf);

    /**
     * 获取对应的反序列化工厂
     *
     * @param url 优先获取 url 的 SERIALIZER 参数对应的 serializer，默认为 hessian
     * @param buf 将从该 buf 中获取必要的数据然后反序列化得到相应的结果
     * @return 真正的反序列化器
     */
    InputSerializer deserializer(URL url, ByteBuf buf);

    /**
     * 序列化器的 id 号，注意这里是一个字节来表示，并且只能使用其低 5 位，也就是说理论上最多支持 2^5 种序列化器
     *
     * @return
     */
    byte getSerializerId(URL url);
}
