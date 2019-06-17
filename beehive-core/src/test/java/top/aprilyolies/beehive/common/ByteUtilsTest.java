package top.aprilyolies.beehive.common;

import org.junit.Test;

/**
 * @Author EvaJohnson
 * @Date 2019-06-17
 * @Email g863821569@gmail.com
 */
public class ByteUtilsTest {
    @Test
    public void testConvert() {
        byte a = 0x48;
        byte b = -128;
        System.out.println((a & 0xff));
        System.out.println(a);
        System.out.println((b & 0xff));
        System.out.println(b);
        System.out.println((int) b);
    }
}
