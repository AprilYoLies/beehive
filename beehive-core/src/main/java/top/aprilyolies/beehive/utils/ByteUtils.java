package top.aprilyolies.beehive.utils;

import java.util.Arrays;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class ByteUtils {
    public static void fillShort(short source, byte[] target, int offset) {
        if (offset + 1 >= target.length)
            throw new IllegalArgumentException("Cant't fill source " + source + " to target " + Arrays.toString(target) + ", there is no enough space to do this");
        target[offset] = (byte) source;
        target[offset + 1] = (byte) (source >>> 8);
    }

    public static void fillLong(long source, byte[] target, int offset) {
        if (offset + 7 >= target.length)
            throw new IllegalArgumentException("Cant't fill source " + source + " to target " + Arrays.toString(target) + ", there is no enough space to do this");
        target[offset + 7] = (byte) (source >>> 56);
        target[offset + 6] = (byte) (source >>> 48);
        target[offset + 5] = (byte) (source >>> 40);
        target[offset + 4] = (byte) (source >>> 32);
        target[offset + 3] = (byte) (source >>> 24);
        target[offset + 2] = (byte) (source >>> 16);
        target[offset + 1] = (byte) (source >>> 8);
        target[offset] = (byte) (source);
    }

    public static void fillInt(int source, byte[] target, int offset) {
        if (offset + 3 >= target.length)
            throw new IllegalArgumentException("Cant't fill source " + source + " to target " + Arrays.toString(target) + ", there is no enough space to do this");
        target[offset + 3] = (byte) (source >>> 24);
        target[offset + 2] = (byte) (source >>> 16);
        target[offset + 1] = (byte) (source >>> 8);
        target[offset] = (byte) (source);
    }

    /**
     * 新建一个长度为 len 的数组，将 src 的内容全部拷贝到新数组中
     *
     * @param src 字节数组源
     * @param len 新字节数组的长度
     * @return 新得到的字节数组
     */
    public static byte[] copyOf(byte[] src, int len) {
        byte[] des = new byte[Math.min(src.length, len)];
        System.arraycopy(src, 0, des, 0, src.length);
        return des;
    }

    /**
     * 从 src 偏移量为 offset 的位置读取四个字节，拼成一个整型返回
     *
     * @param src
     * @param offset
     * @return
     */
    public static int readInt(byte[] src, int offset) {
        if (offset + 3 >= src.length)
            throw new IllegalArgumentException("Cant't read int from " + Arrays.toString(src) + ", there is no enough space to do this");
        return (src[offset] & 0xff) +
                ((src[offset + 1] & 0xff) << 8) +
                ((src[offset + 2] & 0xff) << 16) +
                ((src[offset + 3] & 0xff) << 24);
    }

    /**
     * 从 src 偏移量为 offset 的位置读取八个字节，拼成一个长整型返回
     *
     * @param src
     * @param offset
     * @return
     */
    public static long readLong(byte[] src, int offset) {
        if (offset + 7 >= src.length)
            throw new IllegalArgumentException("Cant't read int from " + Arrays.toString(src) + ", there is no enough space to do this");
        return (src[offset] & 0xffL) +
                ((src[offset + 1] & 0xffL) << 8) +
                ((src[offset + 2] & 0xffL) << 16) +
                ((src[offset + 3] & 0xffL) << 24) +
                ((src[offset + 4] & 0xffL) << 32) +
                ((src[offset + 5] & 0xffL) << 40) +
                ((src[offset + 6] & 0xffL) << 48) +
                ((src[offset + 7] & 0xffL) << 56);
    }
}
