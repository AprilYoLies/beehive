package top.aprilyolies.beehive.extension;

import org.junit.Before;
import org.junit.Test;
import top.aprilyolies.beehive.compiler.JavassistCompiler;
import top.aprilyolies.beehive.extension.testextension.TestExtension;
import top.aprilyolies.beehive.protocol.Protocol;
import top.aprilyolies.beehive.registry.factory.RegistryFactory;
import top.aprilyolies.beehive.utils.ClassUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */
public class ExtensionTest {
    ExtensionLoader<?> extensionLoader;

    @Before
    public void testGetExtensionLoader() {
        extensionLoader = ExtensionLoader.getExtensionLoader(TestExtension.class);
    }

    @Test
    public void testGetDefaultExtensionName() {
        String name = extensionLoader.getDefaultExtensionName();
        System.out.println(name);
    }

    @Test
    public void testGetExtensionSelectorInstance() {
        String code = "package top.aprilyolies.beehive.extension.testextension;\n" +
                "import top.aprilyolies.beehive.extension.ExtensionLoader;\n" +
                "public class TestExtension$Selector implements top.aprilyolies.beehive.extension.testextension.TestExtension {\n" +
                "public java.lang.String methodWithSelectorAnnotationAndUrlParam(java.lang.String arg0, top.aprilyolies.beehive.common.URL arg1, java.lang.String arg2)  {\n" +
                "if (arg1 == null) throw new IllegalArgumentException(\"Parameter url should not be null.\");\n" +
                "top.aprilyolies.beehive.common.URL url = arg1;\n" +
                "String extName = (url.getProtocol() == null ? \"dubbo\" : url.getProtocol());\n" +
                "if (extName == null) throw new IllegalStateException(\"The extension name got from url should not be empty.\");\n" +
                "top.aprilyolies.beehive.extension.testextension.TestExtension extension = (top.aprilyolies.beehive.extension.testextension.TestExtension)ExtensionLoader.getExtensionLoader(top.aprilyolies.beehive.extension.testextension.TestExtension.class).getExtension(extName);\n" +
                "return extension.methodWithSelectorAnnotationAndUrlParam(arg0, arg1, arg2);\n" +
                "}\n" +
                "}";
        ClassLoader classLoader = ClassUtils.getClassLoader(ExtensionTest.class);
        JavassistCompiler compiler = new JavassistCompiler();
        Class<?>[] interfaces1 = compiler.compile(code, classLoader).getInterfaces();

        Object obj = extensionLoader.getExtensionSelectorInstance();
        String name = obj.getClass().getName();
        Class<?>[] interfaces = obj.getClass().getInterfaces();
        TestExtension instance = (TestExtension) obj;
    }

    @Test
    public void testPropertyInjection() {
        TestExtension extension = (TestExtension) extensionLoader.getExtension("test");
        System.out.println(extension.methodWithSelectorAnnotation());
    }

    @Test
    public void testGetProtocolExtension() {
        Protocol registry = ExtensionLoader.getExtensionLoader(Protocol.class).getExtension("registry");
    }

    @Test
    public void test() {
        RegistryFactory registry = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtensionSelectorInstance();
    }

    @Test
    public void testPrototypeExtension() {
        for (int i = 0; i < 10; i++) {
            TestExtension extension = (TestExtension) extensionLoader.getExtension("test");
            System.out.println(extension);
        }
    }
}
