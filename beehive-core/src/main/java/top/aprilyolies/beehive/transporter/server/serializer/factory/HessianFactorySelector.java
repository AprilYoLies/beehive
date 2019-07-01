package top.aprilyolies.beehive.transporter.server.serializer.factory;

import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.extension.annotation.Selector;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
@Selector
public class HessianFactorySelector implements SerializerFactory {
    private ExtensionLoader extensionLoader = ExtensionLoader.getExtensionLoader(SerializerFactory.class);

    private static final Logger logger = Logger.getLogger(HessianFactorySelector.class);

    private SerializerFactory serializerFactory;

    private SerializerFactory deserializerFactory;

    @Override
    public OutputSerializer serializer(URL url, ByteBuf buf) {
        if (this.serializerFactory == null) {
            synchronized (HessianFactorySelector.class) {
                if (this.serializerFactory == null) {
                    serializerFactory = getSerializerFactory(url, true);
                }
            }
        }
        return serializerFactory.serializer(url, buf);
    }

    @Override
    public InputSerializer deserializer(URL url, ByteBuf buf) {
        if (this.deserializerFactory == null) {
            synchronized (HessianFactorySelector.class) {
                if (this.deserializerFactory == null) {
                    deserializerFactory = getSerializerFactory(url, false);
                }
            }
        }
        return deserializerFactory.deserializer(url, buf);
    }

    @Override
    public byte getSerializerId(URL url) {
        if (serializerFactory != null)
            return serializerFactory.getSerializerId(url);
        if (deserializerFactory != null)
            return deserializerFactory.getSerializerId(url);
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
