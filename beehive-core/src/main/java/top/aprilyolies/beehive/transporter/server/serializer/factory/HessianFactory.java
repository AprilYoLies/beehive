package top.aprilyolies.beehive.transporter.server.serializer.factory;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import io.netty.buffer.ByteBuf;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.hessian.HessianDeserializer;
import top.aprilyolies.beehive.transporter.server.serializer.hessian.HessianSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.hessian.InputStreamAdapter;
import top.aprilyolies.beehive.transporter.server.serializer.hessian.OutputStreamAdapter;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class HessianFactory implements SerializerFactory {
    private final byte SERIALIZER_ID = 0x02;

    @Override
    public OutputSerializer serializer(URL url, ByteBuf buf) {
        return new HessianSerializer(new Hessian2Output(new OutputStreamAdapter(buf)));
    }

    @Override
    public InputSerializer deserializer(URL url, ByteBuf buf) {
        return new HessianDeserializer(new Hessian2Input(new InputStreamAdapter(buf)));
    }

    @Override
    public byte getSerializerId(URL url) {
        return SERIALIZER_ID;
    }
}
