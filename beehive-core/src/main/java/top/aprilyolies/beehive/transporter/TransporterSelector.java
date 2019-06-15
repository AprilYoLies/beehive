package top.aprilyolies.beehive.transporter;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.transporter.server.Server;
import top.aprilyolies.beehive.utils.StringUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
@Selector
public class TransporterSelector extends AbstractTransporter {
    private ExtensionLoader<Transporter> extensionLoader = ExtensionLoader.getExtensionLoader(Transporter.class);

    @Override
    public Server bind(URL url) {
        String transporter = extensionLoader.getDefaultExtensionName();
        if (!StringUtils.isEmpty(url.getParameter(UrlConstants.TRANSPORTER)))
            transporter = url.getParameter(UrlConstants.TRANSPORTER);
        return extensionLoader.getExtension(transporter).bind(url);
    }
}
