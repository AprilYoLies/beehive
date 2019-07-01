package top.aprilyolies.beehive.transporter.server.serializer.factory;

import io.netty.buffer.ByteBuf;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.InputStreamAdapter;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.OutputStreamAdapter;
import top.aprilyolies.beehive.transporter.server.serializer.fastjson.FastJsonDeserializer;
import top.aprilyolies.beehive.transporter.server.serializer.fastjson.FastJsonSerializer;

/**
 * @Author EvaJohnson
 * @Date 2019-07-01
 * @Email g863821569@gmail.com
 */
public class FastJsonFactory implements SerializerFactory {
    private final byte SERIALIZER_ID = 0x04;

    @Override
    public OutputSerializer serializer(URL url, ByteBuf buf) {
        return new FastJsonSerializer(new OutputStreamAdapter(buf));
    }

    @Override
    public InputSerializer deserializer(URL url, ByteBuf buf) {
        return new FastJsonDeserializer(new InputStreamAdapter(buf));
    }

    @Override
    public byte getSerializerId(URL url) {
        return SERIALIZER_ID;
    }
}
