package top.aprilyolies.beehive.transporter.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.server.handler.ClientFinalChannelHandler;
import top.aprilyolies.beehive.transporter.server.handler.HeartbeatHandler;
import top.aprilyolies.beehive.transporter.server.handler.NettyDecoderHandler;
import top.aprilyolies.beehive.transporter.server.handler.NettyEncoderHandler;

import java.net.InetSocketAddress;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public class NettyClient extends AbstractClient {
    // 工作线程的数量，默认为 cpu 核心数加 1
    private int WORKER_THREADS = Runtime.getRuntime().availableProcessors() + 1;
    // netty 客户端
    private Bootstrap bootstrap;
    // netty 客户端工作线程组
    private NioEventLoopGroup workers;
    // 默认的连接超时时间
    private final int DEFAULT_CONNECT_TIMEOUT = 3000;
    // 心跳时间间隔
    private final int HEARTBEAT_INTERVAL = 2000;
    // NioSocketChannel
    private Channel channel;

    public NettyClient(URL url) {
        super(url);
    }

    @Override
    protected void openClient() {
        bootstrap = new Bootstrap();    // 这是 netty 的客户端，和 ServerBootStrap 相对应
        workers = new NioEventLoopGroup(WORKER_THREADS);
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
                        // 该处理器用于向服务器发送心跳消息
                        .addLast("client-idle-handler", new IdleStateHandler(HEARTBEAT_INTERVAL, 0, 0, MILLISECONDS))
                        .addLast("heartbeat-handler", new HeartbeatHandler())   // 该处理器主要是对心跳消息进行处理
                        .addLast("handler", new ClientFinalChannelHandler());    // 最后的 handler，就是核心的逻辑处理器
            }
        });
    }

    @Override
    public Channel connect(InetSocketAddress address) {
        try {
            if (!connected) {
                synchronized (NettyClient.class) {
                    if (!connected) {
                        ChannelFuture future = bootstrap.connect(address).sync();
                        if (future.isSuccess()) {
                            Channel channel = future.channel();
                            Channel oldChannel = this.channel;
                            if (oldChannel != null) {
                                oldChannel.close();
                            }
                            this.channel = channel;
                        }
                        connected = true;
                        return this.channel;
                    }
                }
            }
            if (this.channel == null) {
                throw new IllegalStateException("Client's status was connected, but the channel was null");
            }
            return this.channel;
        } catch (Exception e) {
            connected = false;
            if (channel != null) {
                channel.close();
            }
            e.printStackTrace();
            return null;
        }
    }
}
