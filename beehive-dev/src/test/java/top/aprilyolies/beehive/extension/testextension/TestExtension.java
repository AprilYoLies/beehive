package top.aprilyolies.beehive.extension.testextension;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.extension.annotation.Selector;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */
@SPI("test")
public interface TestExtension {
    @Selector
    String methodWithSelectorAnnotation();

    @Selector
    String methodWithSelectorAnnotationAndUrlParam(String param1, URL url, String param2);

    String methodWithoutSelectorAnnotation();
}
