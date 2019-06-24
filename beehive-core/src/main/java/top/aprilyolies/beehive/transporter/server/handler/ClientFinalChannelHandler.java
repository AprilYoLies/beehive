package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.BeehiveThreadFactory;
import top.aprilyolies.beehive.transporter.EventHandleThread;
import top.aprilyolies.beehive.transporter.server.message.MessageType;
import top.aprilyolies.beehive.transporter.server.message.Request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author EvaJohnson
 * @Date 2019-06-20
 * @Email g863821569@gmail.com
 */
public class ClientFinalChannelHandler extends AbstractFinalChannelHandler {
    // TODO 这里是属于每个实例的 executor，构建方式属于硬编码，应该修改为根据 url 参数信息来构建对应的 Executor
    private final ExecutorService executor = new ThreadPoolExecutor(20, 20, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(20), new BeehiveThreadFactory(DEFAULT_THREAD_NAME, true),
            new ThreadPoolExecutor.DiscardPolicy());

    public ClientFinalChannelHandler(URL url) {
        super(url);
    }

    @Override   // 心跳消息的传播
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // handler: NettyClient -> MultiMessageHandler -> HeartBeatHandler -> AllChannelHandler -> （AllChannelHandler 持有）DecodeHandler -> HeaderExchangeHandler -> DubboProtocol$1
            if (logger.isDebugEnabled()) {
                logger.debug("IdleStateEvent triggered, send heartbeat to server");
            }
            Request req = new Request();
            req.setType(MessageType.HEARTBEAT_REQUEST);
            ctx.writeAndFlush(req);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executor.submit(new EventHandleThread(ctx, getUrl(), msg));
    }
}