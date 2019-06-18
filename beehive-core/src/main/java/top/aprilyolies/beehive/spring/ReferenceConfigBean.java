package top.aprilyolies.beehive.spring;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public class ReferenceConfigBean extends AbstractConfig{
    // 服务使用的协议，默认为 beehive
    private String protocol;
    // 待发布服务的接口全限定名
    private String service;
    // 服务 id
    private Integer id;
    // 服务的名字
    private String name;
    // 注册配置 bean
    private RegistryConfigBean registry;

    public RegistryConfigBean getRegistry() {
        return registry;
    }

    public void setRegistry(RegistryConfigBean registry) {
        this.registry = registry;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
