package top.aprilyolies.beehive.extension.testextension;

import top.aprilyolies.beehive.common.URL;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */
public class TestExtensionImpl implements TestExtension {
    TestExtension testExtension;

    public TestExtension getTestExtension() {
        return testExtension;
    }

    public void setTestExtension(TestExtension testExtension) {
        this.testExtension = testExtension;
    }

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
