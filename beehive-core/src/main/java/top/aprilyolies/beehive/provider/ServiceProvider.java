package top.aprilyolies.beehive.provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.spring.RegistryConfigBean;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.StringUtils;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class ServiceProvider extends ServiceConfigBean implements ApplicationListener<ContextRefreshedEvent>,
        InitializingBean, ApplicationContextAware {
    // 用于记录当前 bean 所代表的服务是否已经发布
    private boolean published = false;

    private static final Object publishMonitor = new Object();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 注册关闭监听器
        // 如果 context 是 ConfigurableApplicationContext 接口的实例
        if (applicationContext instanceof ConfigurableApplicationContext) {
            // spring 框架的方法，向 jvm 注册一个关闭钩子函数，在 jvm 关闭时会调用这个钩子函数来关闭 applicationContext
            ((ConfigurableApplicationContext) applicationContext).registerShutdownHook();
        }
        // 注册关闭监听器
        addApplicationListener(new ShutdownHookListener());
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        exportService();
    }

    public void exportService() {
        if (!published) {
            synchronized (publishMonitor) {
                if (!published) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Export service via thread " + Thread.currentThread().getName());
                    }
                    // 将当前实例以 provider_model 的形式保存到 BeehiveContext 中
                    BeehiveContext.unsafePut(UrlConstants.PROVIDER_MODEL, this);
                    // 根据 registry 属性构建 registry urls
                    List<URL> registryUrls = getRegistryUrl(getRegistry());
                    // 将当前实例中的属性填充到 url 的参数中
                    fillParameters(registryUrls, this);
                    // 检查 registry url 是否包含必要的属性
                    checkRegistryUrls(registryUrls);
                    // 进行真正的服务注册
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
                String providerInfo = getProviderInfo(registryUrl);
                registryUrl.putParameter(UrlConstants.PROVIDER, providerInfo);
            }
            if (StringUtils.isEmpty(registryUrl.getParameter(UrlConstants.CATEGORY))) {
                registryUrl.putParameter(UrlConstants.CATEGORY, UrlConstants.PROVIDERS);
            }
        }
    }

    /**
     * 尝试获取 provider 信息，获取的形式为主机地址：端口号，获取失败将会抛出异常
     *
     * @param registryUrl
     * @return
     */
    private String getProviderInfo(URL registryUrl) {
        try {
            String protocol = getProtocol();
            String ipAddress = InetAddress.getLocalHost().getCanonicalHostName();
            String strPort = registryUrl.getParameter(UrlConstants.SERVER_PORT);
            if (!StringUtils.isEmpty(strPort)) {
                try {
                    Integer.parseInt(strPort);
                } catch (NumberFormatException e) {
                    logger.warn("Specified server-port was wrong, use the default server working port " + UrlConstants.SERVICE_PORT);
                    strPort = UrlConstants.SERVICE_PORT;
                }
            } else {
                strPort = UrlConstants.SERVICE_PORT;
            }
            // 优先使用 -D 参数指定的 port
            String port = System.getProperty("port");
            try {
                Integer.parseInt(port);
                strPort = port;
            } catch (NumberFormatException e) {
                logger.warn("Parameter specified by -D was wrong, ignore this parameter");
            }
            return URLEncoder.encode(protocol + "://" + ipAddress + ":" + strPort + "/" + registryUrl.getParameter(UrlConstants.SERVICE));
        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't get provider ip address", e);
        }
    }


    // 向注册中心注册要发布的服务
    private void registryService(List<URL> registryUrls) {
        if (registryUrls == null || registryUrls.size() == 0) {
            logger.warn("None of url was registered");
            return;
        }
        for (URL registryUrl : registryUrls) {
            convertToRegistryUrl(registryUrl);
            protocolSelector.publish(registryUrl);
        }
    }

    /**
     * 用于为当前 bean 填充一些必要属性
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        checkRegistry();
    }

    /**
     * 检查当前 bean 的 registry 属性是否为空，否则从 spring 容器中获取对应的值进行填充
     */
    private void checkRegistry() {
        RegistryConfigBean registry = getRegistry();
        if (registry == null) {
            if (applicationContext != null) {
                Map<String, RegistryConfigBean> registryMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                        RegistryConfigBean.class, false, false);
                if (registryMap.size() > 0) {
                    // 如果 spring 容器中有多个 registry bean，那么优先获取第一个
                    Collection<RegistryConfigBean> registries = registryMap.values();
                    for (RegistryConfigBean reg : registries) {
                        setRegistry(reg);
                        break;
                    }
                }
            }
        }
    }
}
