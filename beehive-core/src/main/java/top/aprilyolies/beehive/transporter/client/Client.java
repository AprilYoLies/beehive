package top.aprilyolies.beehive.transporter.client;

import io.netty.channel.Channel;
import top.aprilyolies.beehive.transporter.server.EndPoint;

import java.net.InetSocketAddress;

/**
 * @Author EvaJohnson
 * @Date 2019-06-20
 * @Email g863821569@gmail.com
 */

/**
 * 此接口代表 transporter 的 client
 */
public interface Client extends EndPoint {
    /**
     * 进行真正的连接操作
     *
     * @param address 远程服务的请求地址
     */
    Channel connect(InetSocketAddress address);

    /**
     * 用于关闭到服务端的连接
     *
     * @return
     */
    boolean disconnect();
}
