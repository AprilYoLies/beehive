package top.aprilyolies.beehive.spring.namespace;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import top.aprilyolies.beehive.spring.RegistryConfigBean;
import top.aprilyolies.beehive.utils.StringUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class RegistryBeanDefinitionParser extends AbstractBeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(RegistryConfigBean.class);
        beanDefinition.setLazyInit(false);
        String id = element.getAttribute("id");
        if (StringUtils.isEmpty(id)) {
            String name = element.getAttribute("name");
            beanDefinition.getPropertyValues().addPropertyValue("name", name);
            if (!isExisted(parserContext, name)) {
                id = name;
            }
        }
        if (!StringUtils.isEmpty(id)) {
            if (isExisted(parserContext, id)) {
                throw new IllegalStateException(String.format("Bean of id %s has existed", id));
            }
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        }
        String address = element.getAttribute("address");
        beanDefinition.getPropertyValues().addPropertyValue("address", address);
        return beanDefinition;
    }
}
