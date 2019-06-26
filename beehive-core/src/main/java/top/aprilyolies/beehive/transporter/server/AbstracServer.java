package top.aprilyolies.beehive.transporter.server;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;


/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public abstract class AbstracServer implements Server {
    protected final Logger logger = Logger.getLogger(getClass());
    // 底层通信的编解码器
    private final String codec;
    // 服务发布的 url
    private final URL url;

    public String getCodec() {
        return codec;
    }

    public URL getUrl() {
        return url;
    }

    public AbstracServer(URL url) {
        this.codec = url.getParameter(UrlConstants.CODEC);
        this.url = url;
        openServer();
    }

    protected abstract void openServer();

}
