package top.aprilyolies.beehive.extension;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.compiler.Compiler;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.injector.PropertyInjector;
import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */
public class ExtensionLoader<T> {
    private static final Logger logger = Logger.getLogger(ExtensionLoader.class);
    // 默认的 extension 配置加载路径
    private final String BEEHIVE_EXTENSION_DIRECTORY = "META-INF/beehive/";
    // ExtensionLoader 缓存
    private static final Map<Class<?>, ExtensionLoader<?>> extensionLoaderCache = new ConcurrentHashMap<>();
    // ExtensionClass 缓存
    private Map<String, Class<T>> extensionClassCache;
    // ExtensionClass 对应的锁监视器
    private final Object extensionClassMonitor = new Object();
    // ExtensionClass 缓存
    private Map<String, T> extensionInstanceCache;
    // ExtensionClass 对应的锁监视器
    private final Object extensionInstanceMonitor = new Object();
    // ExtensionSelectorClass 缓存
    private Class<T> extensionSelectorClassCache;
    // ExtensionSelectorClass 对应的锁监视器
    private final Object extensionSelectorClassMonitor = new Object();
    // ExtensionSelectorInstance 缓存
    private T extensionSelectorInstanceCache;
    // ExtensionSelectorInstance 对应的锁监视器
    private final Object extensionSelectorInstanceMonitor = new Object();
    // ExtensionWrapper 缓存
    private final Set<Class<T>> extensionWrapperCache = new HashSet<>();
    // 当前 ExtensionLoader 所述的类型
    private Class<T> type;
    // 属性注入器
    private PropertyInjector injector;
    // 默认的 extension 名字
    private String defaultExtensionName;

    public ExtensionLoader(Class<T> type) {
        this.type = type;
        this.defaultExtensionName = getDefaultExtensionName();
        injector = type == PropertyInjector.class ? null : ExtensionLoader.getExtensionLoader(PropertyInjector.class).getExtensionSelectorInstance();
    }

    /**
     * 获取 ExtensionSelector
     *
     * @return
     */
    public T getExtensionSelectorInstance() {
        Class<T> extension = this.extensionSelectorClassCache;
        if (extension == null) {
            synchronized (extensionSelectorClassMonitor) {
                if (extensionSelectorClassCache == null) {
                    // 这里不是直接调用 loadExtensionClasses() 是因为可能配置文件未指定 selector，如果是这样，我们就需要通过 javassist 动态生成
                    // extension selector class，并进行缓存
                    extensionSelectorClassCache = createExtensionSelector();
                    extension = this.extensionSelectorClassCache;
                }
            }
        }
        try {
            if (extension == null)
                throw new IllegalStateException("The extension class is null");
            T instance = extension.newInstance();
            injectProperty(instance);
            this.extensionSelectorInstanceCache = instance;
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException("Can't create extension selector instance via class " + extension.getName(), e);
        }
    }

    // 创建 ExtensionSelector，并完成相关属性的注入
    private Class<T> createExtensionSelector() {
        try {
            loadExtensionClasses();
            Class<T> clazz = this.extensionSelectorClassCache;
            if (clazz == null) {
                // 如果没有指定 ExtensionSelector，那么就通过代码生成一个
                clazz = createInterExtensionSelector();
            }
            return clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Can't get the extension selector", e.getCause());
        }
    }

    // 如果没有指定 ExtensionSelector，那么就通过代码生成一个，这就是手动生成 ExtensionSelector 的代码
    private Class<T> createInterExtensionSelector() {
        String code = new CodeGenerator(type).generateCode();
        ClassLoader classLoader = ClassUtils.getClassLoader(ExtensionLoader.class);
        Compiler compilerSelector = ExtensionLoader.getExtensionLoader(Compiler.class).getExtensionSelectorInstance();
        try {
            //noinspection unchecked
            return (Class<T>) compilerSelector.compile(code, classLoader);
        } catch (Exception e) {
            throw new IllegalStateException("Can't create extension instance.", e);
        }
    }

    // 为 instance 注入相关的属性
    private void injectProperty(T instance) {

    }

    // 完成配置项的 classes 的加载，同时对不同类型的 class 进行缓存
    private void loadExtensionClasses() {
        Map<String, Class<T>> cache = this.extensionClassCache;
        // 如果 cache 为空，那么就说明该 ExtensionLoader 还从未进行过配置项的加载
        if (cache == null) {
            synchronized (extensionClassMonitor) {
                if (extensionClassCache == null) {
                    extensionClassCache = doLoadExtensionClasses();
                }
            }
        }
    }

    // 加载相应路径下的 SPI 配置
    private Map<String, Class<T>> doLoadExtensionClasses() {
        Map<String, Class<T>> extensionClasses = new HashMap<>();
        String resourcePath = this.BEEHIVE_EXTENSION_DIRECTORY + type.getName();
        try {
            ClassLoader cl = ClassUtils.getClassLoader(ExtensionLoader.class);
            if (cl != null) {
                Enumeration<URL> resources = cl.getResources(resourcePath);
                if (resources != null) {
                    // 将获取到的每一个资源解析为对应的 ExtensionLoader
                    while (resources.hasMoreElements()) {
                        URL resource = resources.nextElement();
                        parseResource(extensionClasses, resource);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Can't load resources of path: " + resourcePath);
        }
        return extensionClasses;
    }

    // 将获取到的每一个资源解析为对应的 ExtensionLoader
    private void parseResource(Map<String, Class<T>> extensionClasses, URL resource) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                int i = line.indexOf("=");
                if (i < 0) {
                    logger.info("Invalid extension-name and extension-class couple: " + line + ",ignore this record.");
                    continue;
                }
                String extensionName = line.substring(0, i).trim();
                String extensionClassName = line.substring(i + 1).trim();
                try {
                    if (StringUtils.isEmpty(extensionName)) {
                        logger.error("The extension name must not be empty.");
                        continue;
                    }
                    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) Class.forName(extensionClassName);
                    check(clazz, extensionClasses, extensionName);
                } catch (ClassNotFoundException e) {
                    logger.error("Can't load class named " + extensionClassName + ",ignore this record.");
                }
            }
        } catch (IOException e) {
            logger.error("Can't open stream from specified resource" + resource.getPath());
        }
    }

    // 检查加载的 class 是否符合规范，并根据其特点进行缓存
    private void check(Class<T> clazz, Map<String, Class<T>> extensionClasses, String extensionName) {
        try {
            if (!type.isAssignableFrom(clazz)) {
                logger.error("Loaded class must be subtype of " + type.getName());
                return;
            }
            if (clazz.isAnnotationPresent(Selector.class)) {
                if (extensionSelectorClassCache == null) {
                    extensionSelectorClassCache = clazz;
                } else {
                    logger.error("Selector extension class should no more than one, ignore " + clazz.getName() + " extension class");
                }
                return;
            }
            if (isWrapperClass(clazz)) {
                extensionWrapperCache.add(clazz);
                return;
            }
            // 确保获得的 clazz 有默认构造函数
            clazz.getConstructor();
            // 不能存在同名的不同 extension class
            if (extensionClasses.get(extensionName) != null && extensionClasses.get(extensionName) != clazz) {
                logger.error("There is an ambiguous for an extension-name " + extensionName + " matched two " +
                        "different extension class");
            }
            extensionClasses.putIfAbsent(extensionName, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't create instance from class " + clazz.getName());
        }
    }

    // 判断当前 class 是否有以 type 为参数的构造函数
    private boolean isWrapperClass(Class<T> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // 获取 type 的默认 extension 名字
    public String getDefaultExtensionName() {
        String defaultExtensionName = this.defaultExtensionName;
        if (!StringUtils.isEmpty(defaultExtensionName))
            return defaultExtensionName;
        SPI annotation = type.getAnnotation(SPI.class);
        if (annotation == null)
            throw new IllegalStateException("The type of extension must annotated by annotation " + SPI.class.getName());
        return annotation.value();
    }


    /**
     * 获取 ExtensionLoader，优先从缓存中获取
     *
     * @param type 待获取的 ExtensionLoader 类型
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Can't get the extension loader,because the parameter should not be null");

        if (!type.isInterface() || !type.isAnnotationPresent(SPI.class))
            throw new IllegalArgumentException("Can't get the extension loader,because the parameter should be an " +
                    "interface class and annotated by " + SPI.class.getName() + ".");

        // 尝试从缓存中获取 ExtensionLoader
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) extensionLoaderCache.get(type);

        // 没有从缓存中获取到 ExtensionLoader，那么就新建一个并缓存
        if (extensionLoader == null) {
            extensionLoaderCache.putIfAbsent(type, new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<T>) extensionLoaderCache.get(type);
        }

        return extensionLoader;
    }

    // 根据 extensionName 获取 extension 实例，优先从缓存中获取，没有的话，就根据 extension class 进行构建
    public T getExtension(String extensionName) {
        if (extensionInstanceCache == null) {
            extensionInstanceCache = new HashMap<>();
        }
        T instance = extensionInstanceCache.get(extensionName);
        if (instance != null)
            return instance;
        synchronized (extensionInstanceMonitor) {
            if (extensionInstanceCache.get(extensionName) == null) {
                Map<String, Class<T>> cache = this.extensionClassCache;
                try {
                    instance = cache.get(extensionName).newInstance();
                    injectProperty(instance);
                    extensionInstanceCache.putIfAbsent(extensionName, instance);
                } catch (Exception e) {
                    throw new IllegalStateException("Can't get extension instance for extensionName " + extensionName, e.getCause());
                }
            }
        }
        return instance;
    }

    public Set<String> getSupportedExtensions() {
        if (extensionClassCache == null) {
            loadExtensionClasses();
        }
        return extensionClassCache.keySet();
    }
}
