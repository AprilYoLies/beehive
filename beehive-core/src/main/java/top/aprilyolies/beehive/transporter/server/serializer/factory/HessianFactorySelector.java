package top.aprilyolies.beehive.transporter.server.serializer.factory;

import io.netty.buffer.ByteBuf;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.transporter.server.serializer.Serializer;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class HessianFactorySelector implements SerializerFactory {
    private ExtensionLoader extensionLoader = ExtensionLoader.getExtensionLoader(SerializerFactory.class);

    @Override
    public Serializer serializer(URL url, ByteBuf buf) {
        SerializerFactory extension = getSerializerFactory(url);
        return extension.serializer(url, buf);
    }

    @Override
    public Serializer deserializer(URL url, ByteBuf buf) {
        SerializerFactory extension = getSerializerFactory(url);
        return extension.deserializer(url, buf);
    }

    private SerializerFactory getSerializerFactory(URL url) {
        String extName = extensionLoader.getDefaultExtensionName();
        extName = url.getParameterElseDefault(UrlConstants.SERIALIZER, extName);
        return (SerializerFactory) extensionLoader.getExtension(extName);
    }
}
