package top.aprilyolies.beehive.provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.spring.ReferenceConfigBean;
import top.aprilyolies.beehive.spring.RegistryConfigBean;
import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.StringUtils;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public class ServiceConsumer extends ReferenceConfigBean implements FactoryBean {

    @Override
    public Object getObject() throws Exception {
        return registryConsumer();
    }

    private Object registryConsumer() {
        List<URL> registryUrls = getRegistryUrl(getRegistry());
        fillParameters(registryUrls, this);
        checkRegistryUrls(registryUrls);
        return registryConsumer(registryUrls);
    }

    private Object registryConsumer(List<URL> registryUrls) {
        if (registryUrls == null || registryUrls.size() == 0) {
            logger.warn("None of url was registered");
            return null;
        }
        // 此处限定只会取第一个 registry url 作为注册中心
        for (URL registryUrl : registryUrls) {
            convertToRegistryUrl(registryUrl);
            protocolSelector.subscribe(registryUrl);
            String service = registryUrl.getParameter(UrlConstants.SERVICE);
            return BeehiveContext.safeGet(service);
        }
        return null;
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
            if (StringUtils.isEmpty(registryUrl.getParameter(UrlConstants.CONSUMER))) {
                String consumerInfo = getProviderInfo(registryUrl);
                registryUrl.putParameter(UrlConstants.CONSUMER, consumerInfo);
            }
            if (StringUtils.isEmpty(registryUrl.getParameter(UrlConstants.CATEGORY))) {
                registryUrl.putParameter(UrlConstants.CATEGORY, UrlConstants.CONSUMERS);
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
            return URLEncoder.encode(protocol + "://" + ipAddress + ":" + UrlConstants.SERVICE_PORT + "/" + registryUrl.getParameter(UrlConstants.SERVICE));
        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't get provider ip address", e);
        }
    }

    @Override
    public Class<?> getObjectType() {
        String service = getService();
        Class<?> cls = ClassUtils.forName(service);
        return cls;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkRegistry();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void checkRegistry() {
        RegistryConfigBean registry = getRegistry();
        if (registry == null) {
            if (applicationContext != null) {
                Map<String, RegistryConfigBean> registryMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                        RegistryConfigBean.class, false, false);
                if (registryMap.size() > 0) {
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
