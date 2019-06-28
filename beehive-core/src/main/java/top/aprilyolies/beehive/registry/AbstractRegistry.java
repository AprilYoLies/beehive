package top.aprilyolies.beehive.registry;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.protocol.Protocol;
import top.aprilyolies.beehive.proxy.ProxyFactory;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public abstract class AbstractRegistry implements Registry {
    protected final Logger logger = Logger.getLogger(getClass().getName());
    // 该 protocol selector 将会根据 url 来决定使用何种服务发布协议
    Protocol protocolSelector = ExtensionLoader.getExtensionLoader(Protocol.class).getExtensionSelectorInstance();
    // 该 proxy factory selector 将会决定使用那种方式来生成代理类，现在支持 javassist 和 jdk 代理方法
    final ProxyFactory proxyFactorySelector = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getExtensionSelectorInstance();

    @Override
    public void registry(URL url) {
        if (url == null)
            throw new IllegalArgumentException("Can't publish a service for null url");
        try {
            openServer(url);
            createInvoker(url);
            doPublish(url);
        } catch (Exception e) {
            logger.error("publish service failed", e.getCause());
        }
    }

    protected abstract void openServer(URL url);

    protected abstract void createInvoker(URL url);

    protected abstract void doPublish(URL url) throws Exception;
}
