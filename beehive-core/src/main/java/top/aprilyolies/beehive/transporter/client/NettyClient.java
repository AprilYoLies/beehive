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
import top.aprilyolies.beehive.transporter.BeehiveThreadFactory;
import top.aprilyolies.beehive.transporter.server.handler.ClientFinalChannelHandler;
import top.aprilyolies.beehive.transporter.server.handler.HeartbeatHandler;
import top.aprilyolies.beehive.transporter.server.handler.NettyDecoderHandler;
import top.aprilyolies.beehive.transporter.server.handler.NettyEncoderHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    private final int HEARTBEAT_INTERVAL = 20000;
    // 用于缓存已经连接的地址和 channel
    private Map<String, Channel> addressChannel = new HashMap<>();
    // 用于记录进行连接过的线程
    private Set<Thread> threads = new HashSet<>();


    public NettyClient(URL url) {
        super(url);
    }

    @Override
    protected void openClient() {
        bootstrap = new Bootstrap();    // 这是 netty 的客户端，和 ServerBootStrap 相对应
        workers = new NioEventLoopGroup(WORKER_THREADS, new BeehiveThreadFactory("NettyClientWorkers", true));
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
                        .addLast("client-idle-handler", new IdleStateHandler(HEARTBEAT_INTERVAL, 0, 0, TimeUnit.MILLISECONDS))
                        .addLast("heartbeat-handler", new HeartbeatHandler())   // 该处理器主要是对心跳消息进行处理
                        .addLast("handler", new ClientFinalChannelHandler(getUrl()));    // 最后的 handler，就是核心的逻辑处理器
            }
        });
    }

    /**
     * 进行连接到服务器的操作
     *
     * @param address 远程服务的请求地址
     * @return
     */
    @Override
    public Channel connect(InetSocketAddress address) {
        String channelKey = createChannelKey(address);
        try {
            if (!connected || !isAddressAdded(channelKey) || !checkOk(addressChannel.get(channelKey))) {
                synchronized (NettyClient.class) {
                    if (!connected || !isAddressAdded(channelKey) || !checkOk(addressChannel.get(channelKey))) {
                        addressChannel.remove(channelKey);
                        ChannelFuture future = bootstrap.connect(address).sync();
                        // 根据情况对新的 channel 进行缓存，同时要关闭旧的 channel
                        Channel channel = future.channel();
                        if (future.isSuccess()) {
                            connected = true;
                            threads.add(Thread.currentThread());
                            addressChannel.putIfAbsent(channelKey, channel);
                            return channel;
                        }
                    }
                }
            }
            Channel channel = addressChannel.get(channelKey);
            if (channel == null) {
                throw new IllegalStateException("Client's status was connected, but the channel was null");
            }
            return channel;
        } catch (Exception e) {
            connected = false;
            Channel channel = addressChannel.get(channelKey);
            if (channel != null) {
                channel.close();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用于检查 channel 是否是可用状态
     *
     * @param channel
     * @return
     */
    private boolean checkOk(Channel channel) {
        if (channel == null) {
            return false;
        }
        return channel.isOpen() && channel.isActive();
    }

    /**
     * 根据 address 和当前线程构建 channel key
     *
     * @param address 服务器地址
     * @return 构建的 channel key
     */
    private String createChannelKey(InetSocketAddress address) {
        String addressKey = address.toString();
        String threadKey = Thread.currentThread().getName();
        return addressKey + threadKey;
    }

    /**
     * 判断当前 address 是否已经连接过
     *
     * @param adddressKey 服务器地址构建的 key 信息
     * @return
     */
    private boolean isAddressAdded(String adddressKey) {
        Map<String, Channel> addressChannel = this.addressChannel;
        Set<String> addresses = addressChannel.keySet();
        return addresses.contains(adddressKey);
    }

    @Override
    public boolean disconnect() {
        synchronized (NettyClient.class) {
            for (Channel channel : addressChannel.values()) {
                if (channel != null)
                    channel.close();
            }
            connected = false;
            return true;
        }
    }

    @Override
    public void close() {
        if (workers != null && !workers.isShutdown()) {
            workers.shutdownGracefully();
        }
        for (Channel channel : addressChannel.values()) {
            if (channel != null && channel.isActive() && channel.isOpen())
                channel.close();
        }
    }
}
