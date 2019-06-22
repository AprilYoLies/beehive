package top.aprilyolies.namespace;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @Author EvaJohnson
 * @Date 2019-05-13
 * @Email g863821569@gmail.com
 */
public class MybeanParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        RootBeanDefinition mbd = new RootBeanDefinition();
        mbd.setBeanClassName(element.getAttribute("class"));
        String beanName = element.getAttribute("id");
        MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
        mutablePropertyValues.addPropertyValue("name", element.getAttribute("name"));
        mbd.setPropertyValues(mutablePropertyValues);
        parserContext.getRegistry().registerBeanDefinition(beanName, mbd);

        return mbd;
    }
}
