package top.aprilyolies.codec;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import top.aprilyolies.pojo.Message;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Hessian2Input h2i = new Hessian2Input(new InputStreamAdapter(in));
        h2i.setSerializerFactory(Hessian2SerializerFactory.SERIALIZER_FACTORY);
        Object msg = h2i.readObject(Message.class);
        out.add(msg);
    }
}
