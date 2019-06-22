package top.aprilyolies.beehive.utils;

import java.util.Arrays;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class ByteUtils {
    /**
     * 将一个短整型数据的四个字节填充到 target 数组的 offset 处
     *
     * @param src    长整型数据
     * @param offset 偏移量
     */
    public static void fillShort(short src, byte[] target, int offset) {
        if (offset + 1 >= target.length)
            throw new IllegalArgumentException("Cant't fill source " + src + " to target " + Arrays.toString(target) + ", there is no enough space to do this");
        target[offset] = (byte) src;
        target[offset + 1] = (byte) (src >>> 8);
    }

    /**
     * 将一个长整型数据的四个字节填充到 target 数组的 offset 处
     *
     * @param src    长整型数据
     * @param offset 偏移量
     */
    public static void fillLong(long src, byte[] target, int offset) {
        if (offset + 7 >= target.length)
            throw new IllegalArgumentException("Cant't fill source " + src + " to target " + Arrays.toString(target) + ", there is no enough space to do this");
        target[offset + 7] = (byte) (src >>> 56);
        target[offset + 6] = (byte) (src >>> 48);
        target[offset + 5] = (byte) (src >>> 40);
        target[offset + 4] = (byte) (src >>> 32);
        target[offset + 3] = (byte) (src >>> 24);
        target[offset + 2] = (byte) (src >>> 16);
        target[offset + 1] = (byte) (src >>> 8);
        target[offset] = (byte) (src);
    }

    /**
     * 将一个整型数据的四个字节填充到 target 数组的 offset 处
     *
     * @param src    整型数据
     * @param offset 偏移量
     */
    public static void fillInt(int src, byte[] target, int offset) {
        if (offset + 3 >= target.length)
            throw new IllegalArgumentException("Cant't fill source " + src + " to target " + Arrays.toString(target) + ", there is no enough space to do this");
        target[offset + 3] = (byte) (src >>> 24);
        target[offset + 2] = (byte) (src >>> 16);
        target[offset + 1] = (byte) (src >>> 8);
        target[offset] = (byte) (src);
    }

    /**
     * 新建一个长度为 len 的数组，将 src 的内容全部拷贝到新数组中
     *
     * @param src 字节数组源
     * @param len 新字节数组的长度
     * @return 新得到的字节数组
     */
    public static byte[] copyOf(byte[] src, int len) {
        byte[] des = new byte[len];
        System.arraycopy(src, 0, des, 0, Math.min(src.length, len));
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
     * 从 src 偏移量为 offset 的位置读取四个字节，拼成一个整型返回
     *
     * @param src
     * @param offset
     * @return
     */
    public static short readShort(byte[] src, int offset) {
        if (offset + 3 >= src.length)
            throw new IllegalArgumentException("Cant't read int from " + Arrays.toString(src) + ", there is no enough space to do this");
        return (short) ((src[offset] & 0xff) +
                ((src[offset + 1] & 0xff) << 8));
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
