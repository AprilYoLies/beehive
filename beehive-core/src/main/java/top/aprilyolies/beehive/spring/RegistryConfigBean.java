package top.aprilyolies.beehive.spring;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class RegistryConfigBean {
    // 服务的注册地址
    private String[] address;
    // 服务注册的协议，如果 address 没有指定，则使用此协议
    private String protol;

    public String[] getAddress() {
        return address;
    }

    public void setAddress(String[] address) {
        this.address = address;
    }

    public String getProtol() {
        return protol;
    }

    public void setProtol(String protol) {
        this.protol = protol;
    }
}
