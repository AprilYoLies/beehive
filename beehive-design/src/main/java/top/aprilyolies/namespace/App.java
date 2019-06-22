package top.aprilyolies.namespace;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author EvaJohnson
 * @Date 2019-05-13
 * @Email g863821569@gmail.com
 */
public class App {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(new String[]{"beehive.xml"}, false);
        // 刷新配置
        ac.refresh();
        // 获取自定义 bean
        People bean = (People) ac.getBean("people", People.class);
        // 调用对应方法
        System.out.println(bean.getName());
    }
}
