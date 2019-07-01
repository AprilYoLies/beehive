package top.aprilyolies.beehive.spring;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public abstract class ServiceConfigBean extends AbstractConfig {
    // 服务使用的协议，默认为 beehive
    private String protocol;
    // 待发布服务的接口全限定名
    private String service;
    // 服务的实现类
    private Object ref;
    // 服务 id
    private String id;
    // 服务的名字
    private String name;
    // 注册配置 bean
    private RegistryConfigBean registry;
    // 代理创建方式
    private String proxyFactory;
    // 序列化方式
    private String serializer;

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getProxyFactory() {
        return proxyFactory;
    }

    public void setProxyFactory(String proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

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

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
