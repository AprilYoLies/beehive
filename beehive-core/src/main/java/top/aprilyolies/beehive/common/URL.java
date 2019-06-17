package top.aprilyolies.beehive.common;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */
public class URL {
    private final Logger logger = Logger.getLogger(URL.class);
    // 协议
    private String protocol;
    // 主机地址
    private String host;
    // 端口号
    private int port = -1;
    // 路径
    private String path;
    // 参数集合
    private final Map<String, String> parameters = new ConcurrentHashMap<>();

    private URL originUrl;

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        if (parameters != null)
            this.parameters.putAll(parameters);
        this.path = path;
        this.port = port;
    }

    public URL(URL from) {
        this.protocol = from.getProtocol();
        this.host = from.getHost();
        this.port = from.getPort();
        this.parameters.putAll(from.getParameters());
        this.path = from.getPath();
    }

    public URL() {

    }

    public URL getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(URL originUrl) {
        this.originUrl = originUrl;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    // 直接通过 address 构建 URL
    public static URL buildFromAddress(String address) {
        if (address == null || (address = address.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = address.indexOf("?"); // separator between body and parameters
        if (i >= 0) {
            String[] parts = address.substring(i + 1).split("&");
            parameters = new HashMap<>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            address = address.substring(0, i);
        }
        i = address.indexOf("://");
        if (i >= 0) {
            if (i == 0) {
                throw new IllegalStateException("url missing protocol: \"" + address + "\"");
            }
            protocol = address.substring(0, i);
            address = address.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = address.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + address + "\"");
                }
                protocol = address.substring(0, i);
                address = address.substring(i + 1);
            }
        }

        i = address.indexOf("/");
        if (i >= 0) {
            path = address.substring(i + 1);
            address = address.substring(0, i);
        }
        i = address.lastIndexOf("@");
        if (i >= 0) {
            throw new IllegalStateException("Username and password is unsupported");
        }
        i = address.lastIndexOf(":");
        if (i >= 0 && i < address.length() - 1) {
            if (address.lastIndexOf("%") > i) {
                // ipv6 address with scope id
                // e.g. fe80:0:0:0:894:aeec:f37d:23e1%en0
                // see https://howdoesinternetwork.com/2013/ipv6-zone-id
                // ignore
            } else {
                port = Integer.parseInt(address.substring(i + 1));
                address = address.substring(0, i);
            }
        }
        if (address.length() > 0) {
            host = address;
        }
        return new URL(protocol, host, port, path, parameters);
    }

    // 获取 url 的参数
    public String getParameter(String key) {
        return parameters.get(key);
    }

    public void putParameter(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            logger.warn("The key and value should not be null");
        }
        parameters.putIfAbsent(key, value);
    }

    // 复制一个 URL
    public static URL copyFromUrl(URL from) {
        return new URL(from);
    }

    /**
     * 尝试从 parameters 中获取 key 对应的值，如果获取的值为空，那么就返回缺省值
     *
     * @param key 属性的 key
     * @param dft 缺省值
     * @return
     */
    public String getParameterElseDefault(String key, String dft) {
        String s = parameters.get(key);
        if (StringUtils.isEmpty(s)) {
            s = dft;
        }
        return s;
    }

    @Override
    public String toString() {
        return "URL{" +
                "protocol='" + protocol + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                ", parameters=" + parameters +
                ", originUrl=" + originUrl +
                '}';
    }

    public static String serviceKey(URL url) {
        // 获取 url 端口号
        int port = url.getPort();
        // 通过 port、servieName、serviceVersion、serviceGroup 构建 serviceKey
        return serviceKey(port, url.getPath(), url.getParameter(UrlConstants.VERSION_KEY), url.getParameter(UrlConstants.GROUP_KEY));
    }

    private static String serviceKey(int port, String path, String version, String group) {
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

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    public void putParameterIfAbsent(String codec, String defaultCodec) {
        String p = parameters.get(codec);
        if (StringUtils.isEmpty(p)) {
            parameters.putIfAbsent(codec, defaultCodec);
        }
    }
}
