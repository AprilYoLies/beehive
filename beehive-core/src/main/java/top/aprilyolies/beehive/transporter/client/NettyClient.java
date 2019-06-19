package top.aprilyolies.beehive.transporter.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.server.AbstracServer;
import top.aprilyolies.beehive.transporter.server.handler.NettyDecoderHandler;
import top.aprilyolies.beehive.transporter.server.handler.NettyEncoderHandler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public class NettyClient extends AbstracServer {
    // netty 客户端
    private Bootstrap bootstrap;
    // netty 客户端工作线程组
    private NioEventLoopGroup workers;
    // 默认的连接超时时间
    private final int DEFAULT_CONNECT_TIMEOUT = 3000;
    // 心跳时间间隔
    private final int HEARTBEAT_INTERVAL = 2000;

    public NettyClient(URL url) {
        super(url);
    }

    @Override
    protected void openServer() {
        bootstrap = new Bootstrap();    // 这是 netty 的客户端，和 ServerBootStrap 相对应
        bootstrap.group(workers)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout())
                .channel(NioSocketChannel.class);

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT);   // 这里说明连接超时至少要是 3000 毫秒

        bootstrap.handler(new ChannelInitializer() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()//.addLast("logging",new LoggingHandler(LogLevel.INFO))//for debug
                        .addLast("decoder", new NettyDecoderHandler(getUrl()))   // 指定 decoder -> InternalDecoder
                        .addLast("encoder", new NettyEncoderHandler(getUrl()))   // 指定 encoder -> InternalEncoder
                        .addLast("client-idle-handler", new IdleStateHandler(HEARTBEAT_INTERVAL, 0, 0, MILLISECONDS));
//                        .addLast("handler", nettyClientHandler);    // 最后的 handler，就是核心的逻辑处理器
            }
        });
    }
}
