package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.transporter.server.message.MessageType;
import top.aprilyolies.beehive.transporter.server.message.Request;
import top.aprilyolies.beehive.transporter.server.message.Response;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class HeartbeatHandler extends ChannelDuplexHandler {
    private static final Logger logger = Logger.getLogger(HeartbeatHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isHeartbeatRequest(msg)) {
            Request request = (Request) msg;
            Response response = new Response(request.getId());
            response.setType(MessageType.HEARTBEAT_RESPONSE);
            ctx.writeAndFlush(response);
            if (logger.isDebugEnabled()) {
                logger.debug("Received heartbeat request from " + ctx.channel().remoteAddress());
            }
            return;
        }
        if (isHeartbeatResponse(msg)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received heartbeat response from " + ctx.channel().remoteAddress());
            }
            return;
        }
        super.channelRead(ctx, msg);
    }

    private boolean isHeartbeatResponse(Object msg) {
        if (msg instanceof Response) {
            Response response = (Response) msg;
            MessageType type = response.getType();
            return type == MessageType.HEARTBEAT_RESPONSE;
        }
        return false;
    }

    /**
     * 判断接收到的消息是否是 HeartbeatRequest
     *
     * @param msg
     * @return
     */
    private boolean isHeartbeatRequest(Object msg) {
        if (msg instanceof Request) {
            Request request = (Request) msg;
            MessageType type = request.getType();
            return type == MessageType.HEARTBEAT_REQUEST;
        }
        return false;
    }


}
