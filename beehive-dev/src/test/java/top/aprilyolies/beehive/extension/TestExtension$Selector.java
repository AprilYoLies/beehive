package top.aprilyolies.beehive.extension;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */

//package top.aprilyolies.extension.testextension;

import top.aprilyolies.beehive.extension.testextension.TestExtension;

public class TestExtension$Selector implements TestExtension {
    @Override
    public String methodWithSelectorAnnotation() {
        return null;
    }

    public java.lang.String methodWithSelectorAnnotationAndUrlParam(java.lang.String arg0, top.aprilyolies.beehive.common.URL arg1, java.lang.String arg2) {
        if (arg1 == null) throw new IllegalArgumentException("Parameter url should not be null.");
        top.aprilyolies.beehive.common.URL url = arg1;
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null)
            throw new IllegalStateException("The extension name got from url should not be empty.");
        TestExtension extension = (TestExtension) ExtensionLoader.getExtensionLoader(TestExtension.class).getExtension(extName);
        return extension.methodWithSelectorAnnotationAndUrlParam(arg0, arg1, arg2);
    }

    @Override
    public String methodWithoutSelectorAnnotation() {
        return null;
    }
}