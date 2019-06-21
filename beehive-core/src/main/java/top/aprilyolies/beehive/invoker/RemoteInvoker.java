package top.aprilyolies.beehive.invoker;

import io.netty.channel.Channel;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.RpcInfo;
import top.aprilyolies.beehive.common.result.RpcResult;
import top.aprilyolies.beehive.transporter.client.Client;
import top.aprilyolies.beehive.transporter.server.message.MessageType;
import top.aprilyolies.beehive.transporter.server.message.Request;

import java.net.InetSocketAddress;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public class RemoteInvoker extends AbstractInvoker {
    // 用于连接的地址
    private final String host;
    // 用于连接的端口
    private final int port;
    // 连接的客户端
    private final Client client;

    public RemoteInvoker(String host, int port, Client client) {
        this.host = host;
        this.port = port;
        this.client = client;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) {
        if (port < 0) {
            throw new IllegalArgumentException("The port " + port + " is less than 0");
        }
        if (client == null) {
            throw new IllegalStateException("None of client could be use, the client was null");
        }
        // 连接服务器
        Channel ch = connectServer();
        // 构建 request 消息
        Request request = buildRequest(info);
        // 发送消息
        ch.writeAndFlush(request);
        // 拿到请求的 id，用于异步获取响应内容
        String sid = String.valueOf(request.getId());
        // 存根请求结果
        BeehiveContext.unsafePut(sid, new RpcResult());
        // 异步获取相应结果
        return BeehiveContext.unsafeRemove(sid, RpcResult.class).get();
    }

    // 打开到服务端的连接，这里默认公用同一个 client
    private Channel connectServer() {
        InetSocketAddress address = new InetSocketAddress(host, port);
        return client.connect(address);
    }

    /**
     * 构建请求消息
     *
     * @param info
     * @return 请求消息
     */
    private Request buildRequest(InvokeInfo info) {
        RpcInfo rpcInfo = info.buildRpcInfo();
        Request request = new Request();
        request.setType(MessageType.REQUEST);
        request.setData(rpcInfo);
        return request;
    }
}
