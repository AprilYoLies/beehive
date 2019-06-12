package top.aprilyolies.beehive.injector;

import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.SPI;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */
public class SpiPropertyInjector implements PropertyInjector {
    @Override
    public <T> T inject(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            return ExtensionLoader.getExtensionLoader(type).getExtensionSelectorInstance();
        }
        return null;
    }
}
