package top.aprilyolies.beehive.common;


import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */

/**
 * 该类用于存放一些全局性的变量，主要分为两种，一种是线程本地 map，这里将其称为 safeProperties，另外一种是共享 map 这里将其称为 unsafeProperties
 */
public class BeehiveContext {
    private final Logger logger = Logger.getLogger(BeehiveContext.class);
    // 线程本地变量
    @SuppressWarnings("unchecked")
    private static final ThreadLocal<Map<String, Object>> safeProperties = new ThreadLocal();

    // 非线程本地变量
    private static final Map<String, Object> unsafeProperties = new ConcurrentHashMap<>();

    static {
        safeProperties.set(new HashMap<>());
    }

    public static Object safeGet(String key) {
        return safeProperties.get().get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T safeGet(String key, Class<T> type) {
        try {
            return (T) safeProperties.get().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static void safePut(String key, Object value) {
        if (safeGet(key) == null) {
            safeProperties.get().putIfAbsent(key, value);
        }
    }

    public static Object unsafeGet(String key) {
        return unsafeProperties.get(key);
    }

    public static <T> T unsafeGet(String key, Class<T> type) {
        //noinspection unchecked
        return (T) unsafeProperties.get(key);
    }

    public static void unsafePut(String key, Object value) {
        unsafeProperties.putIfAbsent(key, value);
    }


    @SuppressWarnings("unchecked")
    public static <T> T unsafeRemove(String key, Class<T> type) {
        T val = (T) unsafeProperties.remove(key);
        return val;
    }
}
