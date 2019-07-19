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
        prepareServiceUrl(url);
        transporterSelector.bind(url);
    }

    @Override
    public void subscribe(URL url) {
        if (clientCache == null) {
            synchronized (this) {
                if (clientCache == null) {
                    clientCache = doSubscribe(url);
                    BeehiveContext.unsafePut(UrlConstants.CONSUMERS_TRANSPORT, clientCache);
                }
            }
        }
    }

    private Client doSubscribe(URL url) {
        prepareServiceUrl(url);
        return transporterSelector.connect(url);
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
