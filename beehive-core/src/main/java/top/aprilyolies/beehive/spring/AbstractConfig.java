package top.aprilyolies.beehive.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.protocol.Protocol;
import top.aprilyolies.beehive.provider.ServiceProvider;
import top.aprilyolies.beehive.registry.Registry;
import top.aprilyolies.beehive.transporter.server.EndPoint;
import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public abstract class AbstractConfig implements InitializingBean, ApplicationContextAware {
    protected Logger logger = Logger.getLogger(ServiceProvider.class);

    // 此 protocol 实例会根据 URL 参数的协议来进行分发
    protected Protocol protocolSelector = ExtensionLoader.getExtensionLoader(Protocol.class).getExtensionSelectorInstance();
    // Spring 的上下文环境
    protected ApplicationContext applicationContext;

    /**
     * 从 ServiceConfigBean 实例中获取 address 信息，然后将其转换为 URL
     *
     * @param registry ServiceConfigBean 实例，应该是从 spring 容器中获取
     * @return
     */
    protected List<URL> getRegistryUrl(RegistryConfigBean registry) {
        List<URL> urls = new ArrayList<>();
        if (registry == null)
            return urls;
        String[] addresses = registry.getAddress();
        for (String add : addresses) {
            urls.add(URL.buildFromAddress(add));
        }
        return urls;
    }

    /**
     * 此方法用于获取配置类中 String 类型的属性，填充到每一个 registryUrls 的 parameters 属性中
     *
     * @param registryUrls registryUrls 待填充的 registryUrls
     * @param target       填充这个对象中的属性
     */
    protected void fillParameters(List<URL> registryUrls, Object target) {
        Class<?> clazz = getClass();
        Method[] methods = clazz.getMethods();
        try {
            for (URL registryUrl : registryUrls) {
                Map<String, String> parameters = registryUrl.getParameters();
                // 针对每一条 registryUrl 进行属性的填充
                for (Method method : methods) {
                    // 只要是满足 getter 方法的条件，且返回值类型为 String，就将这个属性填充到 registryUrl 的 parameters 属性中
                    if (ClassUtils.isGetterrMethod(method) && method.getReturnType() == String.class) {
                        String property = ClassUtils.method2Property(method);
                        if (!StringUtils.isEmpty(property)) {
                            String value = (String) method.invoke(target);
                            if (!StringUtils.isEmpty(value))
                                parameters.putIfAbsent(property, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Can't fill parameters from " + this);
            e.printStackTrace();
        }
    }

    /**
     * 将原 url 转换为 registry url
     *
     * @param registryUrl 原 url 实例
     */
    protected void convertToRegistryUrl(URL registryUrl) {
        URL originUrl = URL.copyFromUrl(registryUrl);
        registryUrl.setOriginUrl(originUrl);
        registryUrl.setProtocol(UrlConstants.REGISTRY_PROTOCOL);
    }

    /**
     * 为 application context 添加一个 listener
     *
     * @param listener 待添加的监听器
     * @return
     */
    protected boolean addApplicationListener(ApplicationListener listener) {
        try {
            Method method = applicationContext.getClass().getMethod("addApplicationListener", ApplicationListener.class);
            method.invoke(applicationContext, listener);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * beehive 的关闭监听器，它会监听 context 关闭事件，同时关闭相关的组件
     */
    public class ShutdownHookListener implements ApplicationListener {
        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            // 此监听器只会监听 context 关闭事件
            if (event instanceof ContextClosedEvent) {
                BeehiveShutdownHook.closeAll();
            }
        }
    }

    /**
     * Beehive 关闭钩子函数
     */
    public static class BeehiveShutdownHook {
        private BeehiveShutdownHook() {
        }

        // 将其设置为单例
        public static BeehiveShutdownHook BEEHIVE_SHUTDOWN_HOOK = new BeehiveShutdownHook();
        // 用于存储 Registry
        private static List<Registry> registries = new ArrayList<>();
        // 用于存储 EndPoint
        private static List<EndPoint> endPoints = new ArrayList<>();

        // 添加 Registry
        public static void addRegistry(Registry registry) {
            registries.add(registry);
        }

        // 添加 EndPoint
        public static void addEndPoint(EndPoint endPoint) {
            endPoints.add(endPoint);
        }

        // 关闭注册的 Registry
        public static void closeAll() {
            for (Registry registry : registries) {
                registry.close();
            }
            for (EndPoint endPoint : endPoints) {
                endPoint.close();
            }
        }


    }
}

