package top.aprilyolies.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import top.aprilyolies.codec.MessageDecoder;
import top.aprilyolies.codec.MessageEncoder;
import top.aprilyolies.handler.ClientChannelHandler;
import top.aprilyolies.pojo.Message;

import java.net.InetSocketAddress;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class RpcClient {
    private static Integer DEFAULT_WORKER_THREAD = Runtime.getRuntime().availableProcessors() + 1;

    private static Integer DEFAULT_SERVERA_PORT = 7440;

    private static Integer DEFAULT_SERVERB_PORT = 7441;

    public static void main(String[] args) {
        Bootstrap client = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup(DEFAULT_WORKER_THREAD);
        client.group(group)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);

        client.handler(new ClientChannelInitializer());

        try {
            ChannelFuture futureB = client.connect("127.0.0.1", DEFAULT_SERVERA_PORT).sync();
            InetSocketAddress providerB = new InetSocketAddress("192.168.95.201", DEFAULT_SERVERB_PORT);
            ChannelFuture futureA = client.connect(providerB).sync();
            Channel channelA = futureA.channel();
            Channel channelB = futureB.channel();
            for (int i = 0; i < 100; i++) {
                channelA.writeAndFlush(new Message("channelA message"));
                channelB.writeAndFlush(new Message("channelB message"));
                System.out.println(channelA.isActive() + " " + channelA.isOpen());
                System.out.println(channelB.isActive() + " " + channelB.isOpen());
            }
            System.out.println("Client finished the connect and send the message...");
            futureA.channel().closeFuture().sync();
            futureB.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    private static class ClientChannelInitializer extends ChannelInitializer {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline()
                    .addLast("MessageDecoder", new MessageDecoder())
                    .addLast("MessageEncoder", new MessageEncoder())
                    .addLast("ClientChannelHandler", new ClientChannelHandler());
        }
    }
}
