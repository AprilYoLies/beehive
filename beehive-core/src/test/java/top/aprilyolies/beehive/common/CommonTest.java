package top.aprilyolies.beehive.common;

import org.junit.Test;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class CommonTest {
    @Test
    public void testGetAddress() {
        try {
            System.out.println(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetBeanName() {
        System.out.println(StringUtils.getBeanName(ServiceConfigBean.class));
    }
}
