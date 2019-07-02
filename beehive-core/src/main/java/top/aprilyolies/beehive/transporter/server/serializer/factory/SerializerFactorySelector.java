package top.aprilyolies.beehive.transporter.server.serializer.factory;

import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
@Selector
public class SerializerFactorySelector implements SerializerFactory {
    private ExtensionLoader extensionLoader = ExtensionLoader.getExtensionLoader(SerializerFactory.class);

    private static final Logger logger = Logger.getLogger(SerializerFactorySelector.class);

    private Map<String, SerializerFactory> serializerFactories = new HashMap<>();

    @Override
    public OutputSerializer serializer(URL url, ByteBuf buf) {
        SerializerFactory serializerFactory = getSerializerFactory(url);
        return serializerFactory.serializer(url, buf);
    }

    @Override
    public InputSerializer deserializer(URL url, ByteBuf buf) {
        SerializerFactory serializerFactory = getSerializerFactory(url);
        return serializerFactory.deserializer(url, buf);
    }

    /**
     * 尝试从缓存中获取 SerializerFactory，如果没有获取到，那么就根据 url 参数从 extension 中创建一个
     *
     * @param url
     * @return
     */
    private SerializerFactory getSerializerFactory(URL url) {
        String serializerKey = url.getParameter(UrlConstants.SERIALIZER);
        SerializerFactory serializerFactory = serializerFactories.get(serializerKey);
        if (serializerFactory == null) {
            synchronized (SerializerFactorySelector.class) {
                if (serializerFactories.get(serializerKey) == null) {
                    serializerFactory = getSerializerFactory(url, false);
                    assert serializerFactory != null;
                    serializerFactories.put(serializerKey, serializerFactory);
                }
            }
        }
        return serializerFactory;
    }

    @Override
    public byte getSerializerId(URL url) {
        String serializerKey = url.getParameter(UrlConstants.SERIALIZER);
        SerializerFactory serializerFactory = serializerFactories.get(serializerKey);
        if (serializerFactory != null)
            return serializerFactory.getSerializerId(url);
        return getSerializerFactory(url, true).getSerializerId(url);
    }

    private SerializerFactory getSerializerFactory(URL url, boolean isSerializer) {
        String extName = extensionLoader.getDefaultExtensionName();
        extName = url.getParameterElseDefault(UrlConstants.SERIALIZER, extName);
        if (isSerializer)
            logger.info("Beehive use " + extName + " to serialize the object");
        else
            logger.info("Beehive use " + extName + " to deserialize the object");
        return (SerializerFactory) extensionLoader.getExtension(extName);
    }
}
