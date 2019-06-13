package top.aprilyolies.beehive.provider;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.protocol.Protocol;
import top.aprilyolies.beehive.spring.RegistryConfigBean;
import top.aprilyolies.beehive.spring.ServiceConfigBean;

import java.util.ArrayList;
import java.util.List;

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
                    registryService(registryUrls);
                }
                published = true;
            }
        }
    }

    private void registryService(List<URL> registryUrls) {
        if (registryUrls == null || registryUrls.size() == 0) {
            logger.warn("None of url was registered");
            return;
        }
        for (URL registryUrl : registryUrls) {
            protocol.publish(registryUrl);
        }
    }

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
