package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.transporter.server.message.MessageType;
import top.aprilyolies.beehive.transporter.server.message.Request;

/**
 * @Author EvaJohnson
 * @Date 2019-06-20
 * @Email g863821569@gmail.com
 */
public class ClientFinalChannelHandler extends ChannelDuplexHandler {
    private static final Logger logger = Logger.getLogger(ClientFinalChannelHandler.class);

    // 空解析结果
    private final String EMPTY_DATA = "EMPTY_DATA";

    @Override   // 心跳消息的传播
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // handler: NettyClient -> MultiMessageHandler -> HeartBeatHandler -> AllChannelHandler -> （AllChannelHandler 持有）DecodeHandler -> HeaderExchangeHandler -> DubboProtocol$1
            if (logger.isDebugEnabled()) {
                logger.debug("IdleStateEvent triggered, send heartbeat to server");
            }
            Request req = new Request();
            req.setType(MessageType.HEARTBEAT_REQUEST);
            req.setData(EMPTY_DATA);
            ctx.writeAndFlush(req);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
