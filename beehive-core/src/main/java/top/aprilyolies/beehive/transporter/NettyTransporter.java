package top.aprilyolies.beehive.transporter;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.client.Client;
import top.aprilyolies.beehive.transporter.client.NettyClient;
import top.aprilyolies.beehive.transporter.server.NettyServer;
import top.aprilyolies.beehive.transporter.server.Server;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class NettyTransporter extends AbstractTransporter {
    @Override
    public Server bind(URL url) {
        return new NettyServer(url);
    }

    @Override
    public Client connect(URL url) {
        return new NettyClient(url);
    }
}
