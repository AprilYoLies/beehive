package top.aprilyolies.beehive.protocol;


import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.registry.Registry;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public class RegistryProtocol extends AbstractProtocol {
    @Override
    public void publish(URL url) {
        // 这里是获取 Registry 的过程，需要修改协议类型，以得到正确的 Registry 实现类
        Registry registry = this.registry.createRegistry(url.getOriginUrl());
        // 调用真正的 registry 实现类进行服务的注册
        registry.registry(url);
    }

    @Override
    public void subscribe(URL url) {
        // 这里是获取 Registry 的过程，需要修改协议类型，以得到正确的 Registry 实现类
        Registry registry = this.registry.createRegistry(url.getOriginUrl());
        // 将 registry 信息保存到 Beehive Context 中
        BeehiveContext.unsafePut(UrlConstants.REGISTRIES, registry);
        // 调用真正的 registry 实现类进行服务的注册
        registry.registry(url);
    }
}
