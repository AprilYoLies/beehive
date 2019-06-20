package top.aprilyolies.beehive.transporter.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.transporter.server.handler.ServerFinalChannelHandler;
import top.aprilyolies.beehive.transporter.server.handler.NettyDecoderHandler;
import top.aprilyolies.beehive.transporter.server.handler.NettyEncoderHandler;
import top.aprilyolies.beehive.transporter.server.handler.HeartbeatHandler;
import top.aprilyolies.beehive.utils.StringUtils;

import java.net.InetSocketAddress;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class NettyServer extends AbstracServer implements Server {
    // 工作线程的数量，默认为 cpu 核心数加 1
    private int WORKER_THREADS = Runtime.getRuntime().availableProcessors() + 1;
    // 心跳时间间隔
    private int HEARTBEAT_TIMEOUT = 2000;
    // 空闲超时时间
    private int IDLE_TIMEOUT = HEARTBEAT_TIMEOUT * 3;
    // netty server
    private ServerBootstrap bootstrap;
    // reactor 模型的 boss 线程组
    private NioEventLoopGroup bossGroup;
    // reactor 模型的 worker 线程组
    private NioEventLoopGroup workerGroup;
    // ServerSocketChannel
    private Channel channel;

    public NettyServer(URL url) {
        super(url);
    }

    @Override
    protected void openServer() {
        bootstrap = new ServerBootstrap();
        // netty 采用经典的 reactor 模型，所以这里新建两个 NioEventLoopGroup
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));
        // 根据 url 的 iothreads 参数来确定 NioEventLoopGroup 工作线程组的线程数，如果没有获取到，则默认使用 13 个线程
        workerGroup = new NioEventLoopGroup(WORKER_THREADS, new DefaultThreadFactory("NettyServerWorker", true));
        // NettyServer 类实际也是实现了 ChannelHandler 接口，这里通过 NettyServerHandler 对 NettyServer 进行了封装

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 从 url 中获取 idleTimeout 时长，如果 url 参数中没有指定，那么就直接使用三倍的 heartBeat 时长
                        ch.pipeline()//.addLast("logging",new LoggingHandler(LogLevel.INFO))//for debug
                                .addLast("decoder", new NettyDecoderHandler(getUrl()))   // InternalDecoder
                                .addLast("encoder", new NettyEncoderHandler(getUrl()))   // InternalEncoder
                                // 用于检测 channel 空闲状态，条件成立时关闭对应的 channel，相对的客户端的 Idle 处理器则用于发送心跳消息
                                .addLast("server-idle-handler", new IdleStateHandler(0, 0, IDLE_TIMEOUT, MILLISECONDS))
                                .addLast("heartbeat-handler", new HeartbeatHandler())
                                .addLast("final-handler", new ServerFinalChannelHandler(getUrl()));
                    }
                });
        // bind
        InetSocketAddress bindAddress = getBindAddress();
        ChannelFuture channelFuture = bootstrap.bind(bindAddress);
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
        if (logger.isDebugEnabled())
            logger.debug("Server started on " + bindAddress);
    }

    private InetSocketAddress getBindAddress() {
        String address = getUrl().getAddress();
        int i = address.indexOf(":");
        String ip = address;
        String port = "";
        if (i > 0) {
            ip = address.substring(0, i);
            port = address.substring(i + 1);
        }
        if (StringUtils.isEmpty(port))
            port = UrlConstants.SERVICE_PORT;
        return new InetSocketAddress(ip, Integer.parseInt(port));
    }
}
