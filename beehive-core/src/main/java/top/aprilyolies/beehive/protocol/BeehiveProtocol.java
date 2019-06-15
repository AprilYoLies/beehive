package top.aprilyolies.beehive.protocol;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.transporter.server.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
        prepareServiceUrl(url);
        return transporterSelector.bind(url);
    }

    /**
     * 准备 service url，主要是准备一些和服务器启动相关的参数
     *
     * @param url
     */
    private void prepareServiceUrl(URL url) {
        url.putParameterIfAbsent(UrlConstants.CODEC, UrlConstants.DEFAULT_CODEC);
        String providerInfo = url.getParameterElseDefault(UrlConstants.PROVIDER, getServiceHost());
        if (providerInfo.indexOf(":") > 0) {
            providerInfo = providerInfo.substring(0, providerInfo.indexOf(":"));
        }
        String host = providerInfo;
        url.setHost(host);
        url.setPort(Integer.parseInt(UrlConstants.SERVICE_PORT));
    }

    /**
     * 获取服务主机 ip
     *
     * @return
     */
    private String getServiceHost() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't get service host");
        }
    }
}
