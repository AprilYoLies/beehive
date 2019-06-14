package top.aprilyolies.beehive.common;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class CommonTest {
    @Test
    public void testGetAddress () {
        try {
            System.out.println(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ;
    }
}
