package top.aprilyolies.beehive.spring.namespace;

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.StringUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {
    /**
     * 检查在 spring 容器中是否已存在相同 id 的 bean
     *
     * @param parserContext
     * @param name
     * @return
     */
    protected boolean isExisted(ParserContext parserContext, String name) {
        return parserContext.getRegistry().containsBeanDefinition(name);
    }

    /**
     * 根据全限定名获取驼峰名
     *
     * @param fullName 全限定名
     * @return
     */
    protected String getBeanName(String fullName) {
        if (StringUtils.isEmpty(fullName)) {
            return StringUtils.getBeanName(ServiceConfigBean.class);
        }
        return StringUtils.getBeanName(fullName);
    }
}
