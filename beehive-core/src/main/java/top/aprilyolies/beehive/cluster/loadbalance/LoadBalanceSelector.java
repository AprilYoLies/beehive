package top.aprilyolies.beehive.cluster.loadbalance;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
@Selector
public class LoadBalanceSelector extends AbstractLoadBalance {
    private ExtensionLoader extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);

    @Override
    public LoadBalance createLoadBalance(URL url) {
        String defaultExtensionName = extensionLoader.getDefaultExtensionName();
        String extName = url.getParameterElseDefault(UrlConstants.LOAD_BALANCE, defaultExtensionName);
        return (LoadBalance) extensionLoader.getExtension(extName);
    }
}
