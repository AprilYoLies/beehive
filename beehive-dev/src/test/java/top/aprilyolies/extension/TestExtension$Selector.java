package top.aprilyolies.extension;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */

import top.aprilyolies.beehive.extension.ExtensionLoader;

public class TestExtension$Selector implements top.aprilyolies.extension.testextension.TestExtension {
    public java.lang.String methodWithoutSelectorAnnotation() {
        throw new UnsupportedOperationException("Method methodWithoutSelectorAnnotation of interface top.aprilyolies.extension.testextension.TestExtension must annotated with top.aprilyolies.beehive.extension.annotation.Selector and has a top.aprilyolies.beehive.common.URL parameter.");
    }

    public java.lang.String methodWithSelectorAnnotation() {
        throw new UnsupportedOperationException("Method methodWithSelectorAnnotation of interface top.aprilyolies.extension.testextension.TestExtension must annotated with top.aprilyolies.beehive.extension.annotation.Selector and has a top.aprilyolies.beehive.common.URL parameter.");
    }

    public java.lang.String methodWithSelectorAnnotationAndUrlParam(java.lang.String arg0, top.aprilyolies.beehive.common.URL arg1, java.lang.String arg2) {
        if (arg1 == null) throw new IllegalArgumentException("Parameter url should not be null.");
        top.aprilyolies.beehive.common.URL url = arg1;
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null) throw new IllegalStateException("The extension name got from url should not be empty.");
        top.aprilyolies.extension.testextension.TestExtension extension = (top.aprilyolies.extension.testextension.TestExtension) ExtensionLoader.getExtensionLoader(top.aprilyolies.extension.testextension.TestExtension.class).getExtension(extName);
        return extension.methodWithSelectorAnnotationAndUrlParam(arg0, arg1, arg2);
    }
}