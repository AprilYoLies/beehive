package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.invoker.Invoker;
import top.aprilyolies.beehive.utils.StringUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
@Selector
public class ProxyFactorySelector implements ProxyFactory {
    private final ExtensionLoader extensionLoader = ExtensionLoader.getExtensionLoader(ProxyFactory.class);

    @Override
    public <T> Invoker<T> createProxy(URL url) {
        throw new UnsupportedOperationException("This is proxy factory selector, please call" +
                "top.aprilyolies.beehive.proxy.ProxyFactorySelector.createProxyFactory");
    }

    @Override
    public ProxyFactory createProxyFactory(URL url) {
        String extName = extensionLoader.getDefaultExtensionName();
        if (url != null && !StringUtils.isEmpty(url.getParameter(UrlConstants.PROXY_FACTORY))) {
            extName = url.getParameter(UrlConstants.PROXY_FACTORY);
        }
        return (ProxyFactory) extensionLoader.getExtension(extName);
    }

}
