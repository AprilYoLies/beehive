package top.aprilyolies.beehive.transporter.client;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.server.AbstracServer;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public class NettyClient extends AbstracServer {
    public NettyClient(URL url) {
        super(url);
    }

    @Override
    protected void openServer() {

    }
}
