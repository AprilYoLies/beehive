package top.aprilyolies.beehive.transporter;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.spring.AbstractConfig;
import top.aprilyolies.beehive.transporter.client.Client;
import top.aprilyolies.beehive.transporter.client.NettyClient;
import top.aprilyolies.beehive.transporter.server.NettyServer;
import top.aprilyolies.beehive.transporter.server.Server;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class NettyTransporter extends AbstractTransporter {
    private static final Logger logger = Logger.getLogger(NettyTransporter.class);
    // 用于缓存绑定在不同端口的 server
    private final Map<String, Server> serverCache = new HashMap<>();

    @Override
    public Server bind(URL url) {
        String serverKey = buildServerKey(url);
        Server server = serverCache.get(serverKey);
        if (server == null) {
            synchronized (serverCache) {
                if (serverCache.get(serverKey) == null) {
                    server = new NettyServer(url);
                    AbstractConfig.BeehiveShutdownHook.addEndPoint(server);
                    serverCache.putIfAbsent(serverKey, server);
                }
            }
        }
        return server;
    }

    /**
     * 根据 url 构建 server key，用于缓存 server 信息
     *
     * @param url 用于发布服务的 server url
     * @return server key
     */
    private String buildServerKey(URL url) {
        if (url == null)
            throw new IllegalArgumentException("To start a server, url should not be null");
        return url.getHost() + ":" + url.getPort();
    }

    @Override
    public Client connect(URL url) {
        NettyClient nettyClient = new NettyClient(url);
        AbstractConfig.BeehiveShutdownHook.addEndPoint(nettyClient);
        return nettyClient;
    }
}
