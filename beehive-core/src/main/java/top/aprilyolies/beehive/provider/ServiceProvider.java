package top.aprilyolies.beehive.provider;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.protocol.Protocol;
import top.aprilyolies.beehive.spring.RegistryConfigBean;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.StringUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class ServiceProvider extends ServiceConfigBean implements ApplicationListener {
    private Logger logger = Logger.getLogger(ServiceProvider.class);
    // 此 protocol 实例会根据 URL 参数的协议来进行分发
    private Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getExtensionSelectorInstance();
    // 用于记录当前 bean 所代表的服务是否已经发布
    private boolean published = false;

    private static final Object publishMonitor = new Object();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        exportService();
    }

    public void exportService() {
        if (!published) {
            synchronized (publishMonitor) {
                if (!published) {
                    List<URL> registryUrls = getRegistryUrl(getRegistry());
                    fillParameters(registryUrls, this);
                    checkRegistryUrls(registryUrls);
                    registryService(registryUrls);
                }
                published = true;
            }
        }
    }

    /**
     * 对 registryUrl 的相关属性进行检查，必要时抛出异常
     *
     * @param registryUrls
     */
    private void checkRegistryUrls(List<URL> registryUrls) {
        for (URL registryUrl : registryUrls) {
            if (StringUtils.isEmpty(registryUrl.getProtocol()))
                throw new RuntimeException("The protocol of registry url should not be null");
            if (registryUrl.getPort() == -1)
                throw new RuntimeException("The port of registry url should not be a positive number");
            if (StringUtils.isEmpty(registryUrl.getHost()))
                throw new RuntimeException("The host of registry url should not be null");
            if (StringUtils.isEmpty(registryUrl.getPath())) {
                if (registryUrl.getParameter(UrlConstants.SERVICE) == null)
                    throw new RuntimeException("The path of registry url should not be null");
                registryUrl.setPath(registryUrl.getParameter(UrlConstants.SERVICE));
            }
            if (StringUtils.isEmpty(registryUrl.getParameter(UrlConstants.PROVIDER))) {
                String providerInfo = getProviderInfo();
                registryUrl.putParameter(UrlConstants.PROVIDER, providerInfo);
            }
        }
    }

    /**
     * 尝试获取 provider 信息，获取的形式为主机地址：端口号，获取失败将会抛出异常
     *
     * @return
     */
    private String getProviderInfo() {
        try {
            String ipAddress = InetAddress.getLocalHost().getCanonicalHostName();
            return ipAddress + ":" + UrlConstants.SERVICE_PORT;
        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't get provider ip address", e);
        }
    }

    /**
     * 此方法用于获取配置类中 String 类型的属性，填充到每一个 registryUrls 的 parameters 属性中
     *
     * @param registryUrls registryUrls 待填充的 registryUrls
     * @param target       填充这个对象中的属性
     */
    private void fillParameters(List<URL> registryUrls, Object target) {
        Class<? extends ServiceProvider> clazz = getClass();
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

    private void registryService(List<URL> registryUrls) {
        if (registryUrls == null || registryUrls.size() == 0) {
            logger.warn("None of url was registered");
            return;
        }
        for (URL registryUrl : registryUrls) {
            registryUrl = convertToRegistryUrl(registryUrl);
            protocol.publish(registryUrl);
        }
    }

    /**
     * 将原 url 转换为 registry url
     *
     * @param registryUrl 原 url 实例
     * @return 转换后得到的 registry url
     */
    private URL convertToRegistryUrl(URL registryUrl) {
        URL originUrl = URL.copyFromUrl(registryUrl);
        registryUrl.setOriginUrl(originUrl);
        registryUrl.setProtocol(UrlConstants.REGISTRY_PROTOCOL);
        return registryUrl;
    }

    /**
     * 从 ServiceConfigBean 实例中获取 address 信息，然后将其转换为 URL
     *
     * @param registry ServiceConfigBean 实例，应该是从 spring 容器中获取
     * @return
     */
    private List<URL> getRegistryUrl(RegistryConfigBean registry) {
        List<URL> urls = new ArrayList<>();
        if (registry == null)
            return urls;
        String[] addresses = registry.getAddress();
        for (String add : addresses) {
            urls.add(URL.buildFromAddress(add));
        }
        return urls;
    }

}
