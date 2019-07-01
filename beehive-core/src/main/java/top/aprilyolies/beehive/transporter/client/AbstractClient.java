package top.aprilyolies.beehive.transporter.client;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;

/**
 * @Author EvaJohnson
 * @Date 2019-06-20
 * @Email g863821569@gmail.com
 */
public abstract class AbstractClient implements Client {
    protected final Logger logger = Logger.getLogger(getClass());
    // 服务发布的 url
    private final URL url;
    // 连接状态
    protected volatile boolean connected;


    public URL getUrl() {
        return url;
    }

    public AbstractClient(URL url) {
        this.url = url;
        openClient();
    }

    protected abstract void openClient();
}
