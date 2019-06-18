package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.BeehiveThreadFactory;
import top.aprilyolies.beehive.transporter.EventHandleThread;

import java.util.concurrent.*;

/**
 * @Author EvaJohnson
 * @Date 2019-06-17
 * @Email g863821569@gmail.com
 */
public class FinalChannelHandler extends ChannelDuplexHandler {
    private static final Logger logger = Logger.getLogger(ChannelDuplexHandler.class);
    // 默认的 FinalChannelHandler 的线程池线程名
    private static final String DEFAULT_THREAD_NAME = "final-channel-handler-thread";
    // 共享的 executor
    private static final ExecutorService SHARED_EXECUTOR = Executors.newCachedThreadPool(new BeehiveThreadFactory(DEFAULT_THREAD_NAME, true));
    private final URL url;
    // TODO 这里是属于每个实例的 executor，构建方式属于硬编码，应该修改为根据 url 参数信息来构建对应的 Executor
    private final ExecutorService executor = new ThreadPoolExecutor(20, 20, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(20), new BeehiveThreadFactory(DEFAULT_THREAD_NAME, true),
            new ThreadPoolExecutor.DiscardPolicy());

    public FinalChannelHandler(URL url) {
        this.url = url;
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
        if (executor == null || executor.isShutdown()) {
            executor = SHARED_EXECUTOR;
        }
        EventHandleThread eventHandleThread = new EventHandleThread(ctx, url, msg);
        executor.submit(eventHandleThread);
    }
}
