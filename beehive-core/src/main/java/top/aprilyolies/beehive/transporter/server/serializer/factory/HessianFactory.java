package top.aprilyolies.beehive.transporter.server.serializer.factory;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import io.netty.buffer.ByteBuf;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.server.serializer.Serializer;
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
    @Override
    public Serializer serializer(URL url, ByteBuf buf) {
        return new HessianSerializer(new Hessian2Output(new OutputStreamAdapter(buf)));
    }

    @Override
    public Serializer deserializer(URL url, ByteBuf buf) {
        return new HessianDeserializer(new Hessian2Input(new InputStreamAdapter(buf)));
    }
}
