package top.aprilyolies.beehive.protocol;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.server.Server;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class BeehiveProtocol extends AbstractProtocol {

    @Override
    public void publish(URL url) {
        String serverKey = url.getAddress();
        Server server = serverCache.get(serverKey);
        if (server == null) {
            synchronized (this) {
                server = serverCache.get(serverKey);
                if (server == null) {
                    server = doPublish(url);
                    serverCache.put(serverKey, server);
                }
            }
        }
    }

    private Server doPublish(URL url) {
        return transporterSelector.bind(url);
    }
}
