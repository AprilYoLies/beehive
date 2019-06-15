package top.aprilyolies.beehive.transporter.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import top.aprilyolies.beehive.common.URL;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class NettyDecoder extends ByteToMessageDecoder {
    private final URL url;

    public NettyDecoder(URL url) {
        this.url = url;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    }
}
