package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.BeehiveThreadFactory;
import top.aprilyolies.beehive.transporter.EventHandleThread;
import top.aprilyolies.beehive.transporter.server.message.Request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author EvaJohnson
 * @Date 2019-06-17
 * @Email g863821569@gmail.com
 */
public class ServerFinalChannelHandler extends AbstractFinalChannelHandler {
    private final int FINAL_CHANNEL_HANDLER_THREADS = 10;
    // TODO 这里是属于每个实例的 executor，构建方式属于硬编码，应该修改为根据 url 参数信息来构建对应的 Executor
    private final ExecutorService executor = new ThreadPoolExecutor(FINAL_CHANNEL_HANDLER_THREADS,
            // TODO 这里使用的是无界队列，应该修改为有界队列
            FINAL_CHANNEL_HANDLER_THREADS, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new BeehiveThreadFactory(DEFAULT_THREAD_NAME, true),
            // TODO 如果修改为有界队列，那么当队列满之后，采取的拒绝执行策略应该进行指定
            new ThreadPoolExecutor.AbortPolicy());

    public ServerFinalChannelHandler(URL url) {
        super(url);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            logger.info("Received channel idle message, the idle channel will be closed");
            // TODO 需要完成关闭 channel 相关的代码
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ExecutorService executor = this.executor;
        if (executor.isShutdown()) {
            executor = SHARED_EXECUTOR;
        }
        if (msg instanceof Request) {
            EventHandleThread eventHandleThread = new EventHandleThread(ctx, getUrl(), msg);
            executor.submit(eventHandleThread);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        executor.shutdown();
        super.channelInactive(ctx);
    }
}
