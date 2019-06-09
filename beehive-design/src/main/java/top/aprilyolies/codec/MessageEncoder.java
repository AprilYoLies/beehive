package top.aprilyolies.codec;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class MessageEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        Hessian2Output h2o = new Hessian2Output(new OutputStreamAdapter(out));
        h2o.setSerializerFactory(Hessian2SerializerFactory.SERIALIZER_FACTORY);
        h2o.writeObject(msg);
        h2o.flush();
    }
}
