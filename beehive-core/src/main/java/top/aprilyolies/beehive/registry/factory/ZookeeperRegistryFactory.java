package top.aprilyolies.beehive.registry.factory;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.registry.Registry;
import top.aprilyolies.beehive.registry.ZookeeperRegistry;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public class ZookeeperRegistryFactory implements RegistryFactory {
    @Override
    public Registry createRegistry(URL url) {
        return new ZookeeperRegistry(url);
    }
}
