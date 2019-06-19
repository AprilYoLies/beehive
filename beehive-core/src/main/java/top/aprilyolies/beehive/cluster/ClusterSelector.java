package top.aprilyolies.beehive.cluster;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.invoker.Invoker;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
@Selector
public class ClusterSelector implements Cluster {
    private ExtensionLoader extensionLoader = ExtensionLoader.getExtensionLoader(Cluster.class);

    @Override
    public <T> Invoker<T> join(URL url) {
        String extensionName = extensionLoader.getDefaultExtensionName();
        String extName = url.getParameterElseDefault(UrlConstants.CLUSTER, extensionName);
        //noinspection unchecked
        return ((Cluster) extensionLoader.getExtension(extName)).join(url);
    }
}
