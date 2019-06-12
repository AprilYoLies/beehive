package top.aprilyolies.beehive.extension.testextension;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.Selector;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */
@Selector
public class TestExtensionSelector implements TestExtension {

    @Override
    public String methodWithSelectorAnnotation() {
        return "methodWithSelectorAnnotation";
    }

    @Override
    public String methodWithSelectorAnnotationAndUrlParam(String param1, URL url, String param2) {
        return "methodWithSelectorAnnotationAndUrlParam";
    }

    @Override
    public String methodWithoutSelectorAnnotation() {
        return "methodWithoutSelectorAnnotation";
    }
}
