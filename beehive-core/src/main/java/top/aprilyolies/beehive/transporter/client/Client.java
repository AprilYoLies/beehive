package top.aprilyolies.beehive.transporter.client;

import top.aprilyolies.beehive.common.InvokeInfo;

import java.net.InetSocketAddress;

/**
 * @Author EvaJohnson
 * @Date 2019-06-20
 * @Email g863821569@gmail.com
 */

/**
 * 此接口代表 transporter 的 client
 */
public interface Client {
    /**
     * 进行真正的连接操作
     *
     * @param address 远程服务的请求地址
     * @param info    请求的信息
     */
    void connect(InetSocketAddress address, InvokeInfo info);
}
