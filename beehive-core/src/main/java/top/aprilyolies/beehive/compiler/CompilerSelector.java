package top.aprilyolies.beehive.compiler;

import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.utils.StringUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */

/**
 * 该类用于根据条件获取对应的 Compiler
 */
@Selector()
public class CompilerSelector implements Compiler {
    public volatile String compilerName;

    public void setCompilerName(String compilerName) {
        this.compilerName = compilerName;
    }

    /**
     * 根据 extensionName 获取 extension
     *
     * @param code        源代码
     * @param classLoader 类加载器
     * @return
     */
    @Override
    public Class<?> compile(String code, ClassLoader classLoader) {
        ExtensionLoader<Compiler> loader = ExtensionLoader.getExtensionLoader(Compiler.class);
        String extensionName = this.compilerName;
        if (StringUtils.isEmpty(extensionName))
            extensionName = loader.getDefaultExtensionName();
        Compiler compiler = loader.getExtension(extensionName);
        return compiler.compile(code, classLoader);
    }
}
