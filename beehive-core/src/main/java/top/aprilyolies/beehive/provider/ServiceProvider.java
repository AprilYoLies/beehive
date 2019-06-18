package top.aprilyolies.beehive.provider;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class ServiceProvider extends ServiceConfigBean implements ApplicationListener {
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
            if (StringUtils.isEmpty(registryUrl.getParameter(UrlConstants.CATEGORY))) {
                registryUrl.putParameter(UrlConstants.CATEGORY, UrlConstants.PROVIDERS);
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

}
