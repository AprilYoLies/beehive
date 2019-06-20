package top.aprilyolies.beehive.common;

import org.junit.Test;
import top.aprilyolies.beehive.transporter.server.message.Request;

/**
 * @Author EvaJohnson
 * @Date 2019-06-20
 * @Email g863821569@gmail.com
 */
public class RequestIdTest {
    @Test
    public void testRequestId() {
        for (int i = 0; i < 10; i++) {
            Request request = new Request();
            System.out.println("Request id ---> " + request.getId());
        }
    }
}
