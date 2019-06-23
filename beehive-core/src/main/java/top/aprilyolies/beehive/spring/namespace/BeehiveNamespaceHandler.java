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
        // beehive:registry 标签解析器
        registerBeanDefinitionParser("registry", new RegistryBeanDefinitionParser());
        // beehive:service 标签解析器
        registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());
        // beehive:reference 标签解析器
        registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());
    }
}
