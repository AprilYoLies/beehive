package top.aprilyolies.beehive.common;

import org.junit.Test;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class UrlTest {
    @Test
    public void testBuildFromAddress() {
        URL url = URL.buildFromAddress("zookeeper://127.0.0.1:2181");
        System.out.println(url);
    }
}
