package top.aprilyolies.beehive.injector;

import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */
@Selector
public class PropertyInjectorSelector implements PropertyInjector {
    // SpiExtensionFactory 和 SpringExtensionFactory
    private final List<PropertyInjector> injectors;

    public PropertyInjectorSelector() {
        // loader 为 AdaptiveExtensionFactory 实例
        ExtensionLoader<PropertyInjector> loader = ExtensionLoader.getExtensionLoader(PropertyInjector.class);
        List<PropertyInjector> list = new ArrayList<PropertyInjector>();
        // 遍历 org.apache.dubbo.common.extension.ExtensionFactory 对应配置文件下的所指定的类的名字
        for (String name : loader.getSupportedExtensions()) {
            // 通过 loader 获取 Extension 实例添加到 list 集合中
            list.add(loader.getExtension(name));
        }
        injectors = Collections.unmodifiableList(list);
    }

    /**
     * 获取 Extension
     *
     * @param type object type. 为 method 参数的类型
     * @param name object name. 为 setter 方法所对应的属性的名字
     * @param <T>
     * @return
     */
    @Override
    public <T> T inject(Class<T> type, String name) {
        // 这里的 injectors 对应不同的 Extension 加载策略
        // SpiExtensionFactory 和 SpringExtensionFactory
        // 这里可以看出来 AdaptiveExtensionFactory 就类似于一个适配器
        for (PropertyInjector factory : injectors) {
            T extension = factory.inject(type, name);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }
}
