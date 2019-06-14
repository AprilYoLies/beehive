package top.aprilyolies.beehive.extension.testextension;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
//package top.aprilyolies.beehive.registry.factory;

import top.aprilyolies.beehive.extension.ExtensionLoader;

public class RegistryFactory$Selector implements top.aprilyolies.beehive.registry.factory.RegistryFactory {
    public top.aprilyolies.beehive.registry.Registry createRegistry(top.aprilyolies.beehive.common.URL arg0) {
        if (arg0 == null) throw new IllegalArgumentException("Parameter url should not be null.");
        top.aprilyolies.beehive.common.URL url = arg0;
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null) throw new IllegalStateException("The extension name got from url should not be empty.");
        top.aprilyolies.beehive.registry.factory.RegistryFactory extension = (top.aprilyolies.beehive.registry.factory.RegistryFactory) ExtensionLoader.getExtensionLoader(top.aprilyolies.beehive.registry.factory.RegistryFactory.class).getExtension(extName);
        return extension.createRegistry(arg0);
    }
}
