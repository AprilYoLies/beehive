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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    // ExtensionSelector 缓存
    private T extensionSelectorCache;
    // ExtensionSelector 对应的锁监视器
    private final Object extensionSelectorMonitor = new Object();
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
        injector = type == PropertyInjector.class ? null : ExtensionLoader.getExtensionLoader(PropertyInjector.class).getExtensionSelector();
    }

    /**
     * 获取 ExtensionSelector
     *
     * @return
     */
    private T getExtensionSelector() {
        T extension = this.extensionSelectorCache;
        if (extension == null) {
            synchronized (extensionSelectorMonitor) {
                if (extensionSelectorCache == null) {
                    extensionSelectorCache = createExtensionSelector();
                }
            }
        }
        return extension;
    }

    // 创建 ExtensionSelector，并完成相关属性的注入
    private T createExtensionSelector() {
        try {
            loadExtensionClasses();
            T instance = this.extensionSelectorCache;
            if (instance == null) {
                // 如果没有指定 ExtensionSelector，那么就通过代码生成一个
                instance = createInterExtensionSelector();
            }
            injectProperty(instance);
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException("Can't get the extension selector", e.getCause());
        }
    }

    // 如果没有指定 ExtensionSelector，那么就通过代码生成一个，这就是手动生成 ExtensionSelector 的代码
    private T createInterExtensionSelector() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPackageInfo());
        sb.append(getImportInfo());
        sb.append(getClassInfo());
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            sb.append(getMethodInfo(method));
        }
        sb.append("}");
        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }
        String code = sb.toString();
        ClassLoader classLoader = ClassUtils.getClassLoader(ExtensionLoader.class);
        Compiler compilerSelector = ExtensionLoader.getExtensionLoader(Compiler.class).getExtensionSelector();
        try {
            //noinspection unchecked
            return (T) compilerSelector.compile(code, classLoader).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't create extension instance.", e);
        }
    }

    private String getMethodInfo(Method method) {
        String returnType = method.getReturnType().getCanonicalName();
        String methodName = method.getName();
        String methodContent = getMethodContent(method);
        String methodArgs = getMethodArgsInfo(method);
        String exceptionInfo = getExceptionInfo(method);
        return String.format("public %s %s() %s {\n%s}\n", returnType, methodName, methodArgs, exceptionInfo, methodContent);
    }

    // 获取抛出的异常串
    private String getExceptionInfo(Method method) {
        Class<?>[] ets = method.getExceptionTypes();
        String list = "";
        if (ets.length > 0) {
            list = Arrays.stream(ets).map(Class::getCanonicalName).collect(Collectors.joining(", "));
            return String.format("throws %s", list);
        }
        return list;
    }

    // 拼凑方法参数串
    private String getMethodArgsInfo(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return IntStream.range(0, pts.length)
                .mapToObj(i -> String.format("%s arg%d", pts[i].getCanonicalName(), i))
                .collect(Collectors.joining(","));
    }

    // 生成方法的内容信息
    private String getMethodContent(Method method) {
        Selector anno = method.getAnnotation(Selector.class);
        StringBuffer sb = new StringBuffer();
        if (anno == null) {
            sb.append(getUnsupportedInfo(method));
        } else {
            int urlParaIndex = getUrlParaIndex(method);
            if (urlParaIndex != -1) {
                sb.append(getUrlCheck(urlParaIndex));
            } else {
                sb.append(getUnsupportedInfo(method));
            }
            sb.append(getExtensionNameInfo());
            sb.append(getExtensionNameCheck());
            sb.append(getExtensionAssignment());
            sb.append(getReturnInfo(method));
        }
        return sb.toString();
    }

    private String getReturnInfo(Method method) {
        String returnStatement = method.getReturnType().equals(void.class) ? "" : "return ";
        // 获取方法的参数，用 "，" 进行分割
        String args = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.joining(", "));
        // 拼接方法返回代码，调用 extension 的 method 方法
        return returnStatement + String.format("extension.%s(%s);\n", method.getName(), args);
    }

    private String getExtensionAssignment() {
        return String.format("%s extension = (%s)ExtensionLoader.getExtensionLoader(%s.class).getExtension(extName);\n", type.getName(),
                type.getName(), type.getName());
    }

    private String getExtensionNameCheck() {
        return "if (extName == null) throw new IllegalStateException(\"The extension name got from url should not be empty.\")\n";
    }

    private String getExtensionNameInfo() {
        return "String extName = (url.getProtocol() == null ? \"dubbo\" : url.getProtocol());\n";
    }

    private String getUrlCheck(int urlParaIndex) {
        return String.format("if (arg%d == null) throw new IllegalArgumentException(\"Parameter url should not be null.\");\n%s " +
                "url = arg%d;\n", urlParaIndex, top.aprilyolies.beehive.common.URL.class.getName(), urlParaIndex);
    }

    // 找到 Selector 注解的方法 URL 参数的位置索引
    private int getUrlParaIndex(Method method) {
        int urlParaIndex = -1;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == top.aprilyolies.beehive.common.URL.class) {
                urlParaIndex = i;
                break;
            }
        }
        return urlParaIndex;
    }

    private String getUnsupportedInfo(Method method) {
        return String.format("throw new UnsupportedOperationException(\"Method %s of interface %s must annotated with " +
                        "%s and has a %s parameter.\");\n", method.getName(), type.getCanonicalName(), Selector.class.getName(),
                top.aprilyolies.beehive.common.URL.class.getName());
    }

    private String getClassInfo() {
        // 对于数组或者内部类 getCanonicalName 获取的名字才是我们所理解的名字
        return String.format("public class %s$Selector implements %s {", type.getSimpleName(), type.getCanonicalName());
    }

    private String getImportInfo() {
        return String.format("import %s;\n", ExtensionLoader.class.getName());
    }

    private String getPackageInfo() {
        return String.format("package %s;\n", type.getPackage());
    }

    // 为 instance 注入相关的属性
    private void injectProperty(T instance) {

    }

    // 完成配置项的 classes 的加载，同时对不同类型的 class 进行缓存
    private void loadExtensionClasses() {
        Map<String, Class<T>> cache = this.extensionClassCache;
        // 如果 cache 为空，那么就说明该 ExtensionLoa 还从未进行过配置项的加载
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
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            logger.error("Can't open stream from specified resource" + resource.getPath());
        }
    }

    // 检查加载的 class 是否符合规范，并根据其特点进行缓存
    private void check(Class<T> clazz, Map<String, Class<T>> extensionClasses, String extensionName) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        if (!type.isAssignableFrom(clazz)) {
            logger.error("Loaded class must be subtype of " + type.getName());
            return;
        }
        if (clazz.isAnnotationPresent(Selector.class)) {
            if (extensionSelectorCache == null) {
                extensionSelectorCache = clazz.newInstance();
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
        if (extensionClasses.get(extensionName) != clazz) {
            logger.error("There is an ambiguous for an extension-name " + extensionName + " matched two " +
                    "different extension class");
        }
        extensionClasses.putIfAbsent(extensionName, clazz);
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
        T instance = extensionInstanceCache.get(extensionName);
        if (instance != null)
            return instance;
        synchronized (extensionInstanceMonitor) {
            if (extensionInstanceCache.get(extensionName) == null) {
                Map<String, Class<T>> cache = this.extensionClassCache;
                try {
                    instance = cache.get(extensionName).newInstance();
                    extensionInstanceCache.putIfAbsent(extensionName, instance);
                } catch (Exception e) {
                    throw new IllegalStateException("Can't get extension instance for extensionName " + extensionName, e.getCause());
                }
            }
        }
        return instance;
    }
}
