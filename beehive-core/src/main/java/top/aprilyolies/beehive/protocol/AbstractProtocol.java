package top.aprilyolies.beehive.protocol;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.registry.factory.RegistryFactory;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public abstract class AbstractProtocol implements Protocol {
    protected final Logger logger = Logger.getLogger(getClass());
    // 注册器选择器
    protected final RegistryFactory registry = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtensionSelectorInstance();

}
