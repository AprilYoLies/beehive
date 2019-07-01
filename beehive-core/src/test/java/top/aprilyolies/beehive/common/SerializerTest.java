package top.aprilyolies.beehive.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.factory.SerializerFactory;

import java.io.IOException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-17
 * @Email g863821569@gmail.com
 */
public class SerializerTest {
    private SerializerFactory extensionSelector;
    private URL url;
    private ByteBuf buf;

    @Before
    public void beforeTest() {
        url = new URL();
        url.putParameter(UrlConstants.SERIALIZER, "hessian");
        ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
        buf = allocator.buffer(200);
        extensionSelector = ExtensionLoader.getExtensionLoader(SerializerFactory.class).getExtensionSelectorInstance();
    }

    @Test
    public void testStringSerialization() throws IOException {
        OutputSerializer serializer = extensionSelector.serializer(url, buf);
        InputSerializer deserializer = extensionSelector.deserializer(url, buf);
        serializer.writeUTF("Hello world..");
        serializer.flushBuffer();
        String str = deserializer.readUTF();
        System.out.println(str);
    }

    @Test
    public void testObjectSerialization() throws IOException, ClassNotFoundException {
        OutputSerializer serializer = extensionSelector.serializer(url, buf);
        InputSerializer deserializer = extensionSelector.deserializer(url, buf);
        Class<SerializerTest> clazz = SerializerTest.class;
        serializer.writeObject(clazz);
        serializer.flushBuffer();
        Class cls = deserializer.readObject(Class.class);
        System.out.println(cls.getName());
    }

    @Test
    public void testFastJson() {
        String str = "content";
        byte[] bytes = JSON.toJSONBytes(str, SerializerFeature.EMPTY);
        Object parse = JSON.parse(bytes);
        Object object = JSON.parseObject(bytes, String.class);
        System.out.println(parse);
        System.out.println(object);
    }
}
