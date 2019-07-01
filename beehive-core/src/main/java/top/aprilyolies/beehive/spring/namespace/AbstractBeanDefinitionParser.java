package top.aprilyolies.beehive.spring.namespace;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
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

    /**
     * 解析 element 属性，如果属性值不为空就直接使用，否则使用默认值
     *
     * @param element        待解析的标签元素
     * @param beanDefinition 用于承载解析结果的容器
     * @param name           属性名
     * @param dft            默认值
     */
    protected void parseAttribute(Element element, RootBeanDefinition beanDefinition, String name, String dft) {
        String proxy = element.getAttribute(name);
        if (StringUtils.isEmpty(proxy)) {
            proxy = dft;
        }
        String cameName = getCamelName(name);
        beanDefinition.getPropertyValues().addPropertyValue(cameName, proxy);
    }

    /**
     * 将 xxx-xxx-xxx 格式的名字修改为 xxxXxxXxx 格式
     *
     * @param name
     * @return
     */
    private String getCamelName(String name) {
        String[] strs = name.split("-");
        StringBuilder sb = new StringBuilder();
        if (strs.length <= 1) {
            return strs[0];
        } else {
            sb.append(strs[0]);
            for (int i = 1; i < strs.length; i++) {
                sb.append(strs[i].substring(0, 1).toUpperCase()).append(strs[i].substring(1));
            }
            return sb.toString();
        }
    }
}
