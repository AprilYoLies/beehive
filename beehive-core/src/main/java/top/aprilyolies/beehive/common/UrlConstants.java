package top.aprilyolies.beehive.common;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public class UrlConstants {
    // 服务在 zookeeper 路径下的组信息
    public static final String GROUP_KEY = "group";
    // 服务的版本信息
    public static final String VERSION_KEY = "version";
    // 服务的提供者信息
    public static final String PROVIDER = "provider";
    // 服务的消费者信息
    public static final String CONSUMER = "consumer";
    // 路径分隔符
    public static final String PATH_SEPARATOR = "/";
    // registry 协议
    public static final String REGISTRY_PROTOCOL = "registry";
    // 默认分组
    public static final String DEFAULT_GROUP = "beehive";
    // 服务参数
    public static final String SERVICE = "service";
    // 默认服务端口号
    public static final String SERVICE_PORT = "7440";
    // 用于表示注册的类型
    public static final String CATEGORY = "category";
    // 表示分类的 provider 类型
    public static final String PROVIDERS = "providers";
    // 表示分类的 consumer 类型
    public static final String CONSUMERS = "consumers";
    // 表示分类的 provider 类型
    public static final String SERVICE_REF = "ref";
    // 代理工厂
    public static final String PROXY_FACTORY = "proxy_factory";
    // 服务发布使用的协议，这个参数将会决定服务调用的性能
    public static final String SERVICE_PROTOCOL = "service_protocol";
    // 默认的服务发布协议，该协议会使用 netty 作为底层通信
    public static final String DEFAULT_SERVICE_PROTOCOL = "beehive";
    // 默认的服务发布协议，该协议会使用 netty 作为底层通信
    public static final String TRANSPORTER = "transporter";
    // 通信的编解码器
    public static final String CODEC = "codec";
    // 通默认的编解码器
    public static final String DEFAULT_CODEC = "hessian";
    // beehive 底层的序列化器
    public static final String SERIALIZER = "serializer";
}
