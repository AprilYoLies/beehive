package top.aprilyolies.beehive.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */
public class URL {
    // 协议
    private String protocol;
    // 主机地址
    private String host;
    // 端口号
    private int port;
    // 路径
    private String path;
    // 参数集合
    private Map<String, String> parameters;

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.parameters = parameters;
        this.path = path;
        this.port = port;
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

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
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

    public String getParameter(String key) {
        return parameters.get(key);
    }
}
