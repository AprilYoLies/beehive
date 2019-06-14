package top.aprilyolies.beehive.registry;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.utils.StringUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public abstract class AbstractRegistry implements Registry {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void registry(URL url) {
        if (url == null)
            throw new IllegalArgumentException("Can't publish a service for null url");
        try {
            doPublish(url);
        } catch (Exception e) {
            logger.error("publish service failed", e.getCause());
        }
    }

    protected abstract void doPublish(URL url) throws Exception;

    protected String serviceKey(URL url) {
        // 获取 url 端口号
        int port = url.getPort();
        // 通过 port、servieName、serviceVersion、serviceGroup 构建 serviceKey
        return serviceKey(port, url.getPath(), url.getParameter(UrlConstants.VERSION_KEY), url.getParameter(UrlConstants.GROUP_KEY));
    }

    private String serviceKey(int port, String path, String version, String group) {
        StringBuilder buf = new StringBuilder();
        if (!StringUtils.isEmpty(group)) {
            buf.append(group);
            buf.append("/");
        }
        buf.append(path);
        if (version != null && version.length() > 0 && !"0.0.0".equals(version)) {
            buf.append(":");
            buf.append(version);
        }
        buf.append(":");
        buf.append(port);
        return buf.toString();
    }
}
