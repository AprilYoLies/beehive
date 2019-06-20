package top.aprilyolies.beehive.invoker;

import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.result.Result;
import top.aprilyolies.beehive.transporter.client.Client;

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
    protected Result doInvoke(InvokeInfo info) {
        if (port < 0) {
            throw new IllegalArgumentException("The port " + port + " is less than 0");
        }
        if (client == null) {
            throw new IllegalStateException("None of client could be use, the client was null");
        }
        InetSocketAddress address = new InetSocketAddress(host, port);
        client.connect(address, info);
        return null;
    }
}
