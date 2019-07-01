package top.aprilyolies.beehive.spring;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public abstract class ReferenceConfigBean extends AbstractConfig {
    // 服务使用的协议，默认为 beehive
    private String protocol;
    // 待发布服务的接口全限定名
    private String service;
    // 服务 id
    private String id;
    // 服务的名字
    private String name;
    // 注册配置 bean
    private RegistryConfigBean registry;
    // 负载均衡策略
    private String loadBalance;
    // 代理创建方式
    private String proxyFactory;
    // 序列化方式
    private String serializer;
    // 从 RpcResult 中获取 rpc 结果的最长等待时间
    private String readTimeout;

    public String getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(String readTimeout) {
        this.readTimeout = readTimeout;
    }

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

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
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
