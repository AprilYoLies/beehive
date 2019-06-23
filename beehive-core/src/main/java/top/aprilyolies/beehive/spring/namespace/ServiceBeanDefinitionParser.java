package top.aprilyolies.beehive.spring.namespace;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import top.aprilyolies.beehive.provider.ServiceProvider;
import top.aprilyolies.beehive.utils.StringUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class ServiceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(ServiceProvider.class);
        beanDefinition.setLazyInit(false);
        String id = element.getAttribute("id");
        String service = element.getAttribute("service");
        beanDefinition.getPropertyValues().addPropertyValue("service", service);
        if (StringUtils.isEmpty(id)) {
            String name = element.getAttribute("name");
            beanDefinition.getPropertyValues().addPropertyValue("name", name);
            if (!isExisted(parserContext, name)) {
                id = name;
            } else {
                String beanName = getBeanName(service);
                id = beanName;
                int count = 1;
                while (isExisted(parserContext, id)) {
                    id = beanName + count;
                }
            }
        }
        if (!StringUtils.isEmpty(id)) {
            if (isExisted(parserContext, id)) {
                throw new IllegalStateException(String.format("Bean of id %s has existed", id));
            }
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        }
        String protocol = element.getAttribute("protocol");
        if (StringUtils.isEmpty(protocol)) {
            protocol = "beehive";
        }
        beanDefinition.getPropertyValues().addPropertyValue("protocol", protocol);
        String ref = element.getAttribute("ref");
        // 如果 setter 方法对应的属性为 ref，并且 spring 容器中已经注册过这个 ref 所引用的 bean 的 beanDefinition
        if (parserContext.getRegistry().containsBeanDefinition(ref)) {
            // 那就拿到这个 beanDefinition
            BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(ref);
            // 这个 beanDefinition 必须是单例的
            if (!refBean.isSingleton()) {
                throw new IllegalStateException("The exported service ref " + ref + " must be singleton! Please set the " + ref + " bean scope to singleton, eg: <bean id=\"" + ref + "\" scope=\"singleton\" ...>");
            }
        }
        RuntimeBeanReference reference = new RuntimeBeanReference(ref);
        beanDefinition.getPropertyValues().addPropertyValue("ref", reference);
        return beanDefinition;
    }
}
