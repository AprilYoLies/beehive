package top.aprilyolies.extension;

import org.junit.Before;
import org.junit.Test;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.extension.testextension.TestExtension;

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
        extensionLoader.getDefaultExtensionName();
    }

    @Test
    public void test() {
        extensionLoader.getExtensionSelectorInstance();
    }
}
