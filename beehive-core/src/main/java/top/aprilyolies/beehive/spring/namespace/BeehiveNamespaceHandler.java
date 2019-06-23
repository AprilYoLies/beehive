package top.aprilyolies.beehive.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class BeehiveNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("registry", new RegistryBeanDefinitionParser());
        registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());
//        registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());
    }
}
