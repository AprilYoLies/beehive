package top.aprilyolies.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @Author EvaJohnson
 * @Date 2019-05-13
 * @Email g863821569@gmail.com
 */
public class BeehiveNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("mybean", new MybeanParser());
    }
}
