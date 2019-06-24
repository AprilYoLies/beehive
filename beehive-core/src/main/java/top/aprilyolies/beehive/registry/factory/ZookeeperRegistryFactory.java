package top.aprilyolies.beehive.registry.factory;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.registry.Registry;
import top.aprilyolies.beehive.registry.ZookeeperRegistry;
import top.aprilyolies.beehive.spring.AbstractConfig;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public class ZookeeperRegistryFactory implements RegistryFactory {
    @Override
    public Registry createRegistry(URL url) {
        Registry registry = new ZookeeperRegistry(url);
        // 向 BeehiveShutdownHook 进行注册，以便程序退出时能够关闭 Registry
        AbstractConfig.BeehiveShutdownHook.addRegistry(registry);
        return registry;
    }
}
