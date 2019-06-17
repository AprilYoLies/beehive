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
}
