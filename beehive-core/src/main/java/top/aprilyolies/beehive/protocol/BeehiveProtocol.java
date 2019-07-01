package top.aprilyolies.beehive.protocol;

import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.transporter.client.Client;
import top.aprilyolies.beehive.transporter.server.Server;

import java.net.URLDecoder;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class BeehiveProtocol extends AbstractProtocol {

    @Override
    public void publish(URL url) {
        String serverKey = url.getParameter(UrlConstants.SERVICE);
        Server server = serverCache.get(serverKey);
        if (server == null) {
            synchronized (this) {
                serverCache.computeIfAbsent(serverKey, k -> doPublish(url));
            }
        }
    }

    @Override
    public void subscribe(URL url) {
        String serverKey = url.getParameter(UrlConstants.SERVICE);
        Client client = clientCache.get(serverKey);
        if (client == null) {
            synchronized (this) {
                clientCache.computeIfAbsent(serverKey, k -> doSubscribe(url));
                BeehiveContext.unsafePut(UrlConstants.CONSUMERS_TRANSPORT, clientCache);
            }
        }
    }

    private Client doSubscribe(URL url) {
        prepareServiceUrl(url);
        return transporterSelector.connect(url);
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
        String serviceInfo;
        if (url.isProvider())
            serviceInfo = url.getParameter(UrlConstants.PROVIDER);
        else
            serviceInfo = url.getParameter(UrlConstants.CONSUMER);
        URL providerUrl = URL.buildFromAddress(URLDecoder.decode(serviceInfo));
        int port = providerUrl.getPort();
        if (port < 0) {
            port = 7440;
        }
        url.setPort(port);
        url.setHost(providerUrl.getHost());
    }

}
