package top.aprilyolies.beehive.common;

import org.junit.Test;
import top.aprilyolies.beehive.utils.ByteUtils;

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

    @Test
    public void testFillAndReadShort() {
        short s = 0x1234;
        byte[] target = new byte[20];
        ByteUtils.fillShort(s, target, 10);
        short res = ByteUtils.readShort(target, 10);
        System.out.println(s == res);
    }

    @Test
    public void testFillAndReadInt() {
        int s = 0x1234;
        byte[] target = new byte[20];
        ByteUtils.fillInt(s, target, 10);
        int res = ByteUtils.readInt(target, 10);
        System.out.println(s == res);
    }

    @Test
    public void testFillAndReadLong() {
        long s = 0x1234;
        byte[] target = new byte[20];
        ByteUtils.fillLong(s, target, 10);
        long res = ByteUtils.readLong(target, 10);
        System.out.println(s == res);
    }
}
