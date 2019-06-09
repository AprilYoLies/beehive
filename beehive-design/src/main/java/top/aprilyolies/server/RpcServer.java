package top.aprilyolies.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import top.aprilyolies.codec.MessageDecoder;
import top.aprilyolies.codec.MessageEncoder;
import top.aprilyolies.handler.ServerChannelHandler;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class RpcServer {
    private static Integer DEFAULT_WORKER_THREAD = Runtime.getRuntime().availableProcessors() + 1;

    private static Integer DEFAULT_SERVER_PORT = 7440;

    public static void main(String[] args) {
        ServerBootstrap server = new ServerBootstrap();

        NioEventLoopGroup boss = new NioEventLoopGroup(1);

        NioEventLoopGroup worker = new NioEventLoopGroup(DEFAULT_WORKER_THREAD);

        server.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, false)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ServerChannelInitializer());

        ChannelFuture channelFuture = server.bind("127.0.0.1", DEFAULT_SERVER_PORT);

        System.out.println("Server started on 127.0.0.1:" + DEFAULT_SERVER_PORT);

        channelFuture.syncUninterruptibly();
    }

    private static class ServerChannelInitializer extends ChannelInitializer {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline()
                    .addLast("MessageDecoder", new MessageDecoder())
                    .addLast("MessageEncoder", new MessageEncoder())
                    .addLast("ServerChannelHandler", new ServerChannelHandler());
        }
    }
}
