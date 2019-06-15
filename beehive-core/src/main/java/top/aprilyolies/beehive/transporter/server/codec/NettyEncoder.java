package top.aprilyolies.beehive.transporter.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import top.aprilyolies.beehive.common.URL;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class NettyEncoder extends MessageToByteEncoder {
    private final URL url;

    public NettyEncoder(URL url) {
        this.url = url;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

    }
}
