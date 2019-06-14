package top.aprilyolies.beehive.common;


import org.apache.log4j.Logger;

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

    public static final BeehiveContext BEEHIVE_CONTEXT = new BeehiveContext();

    // 线程本地变量
    @SuppressWarnings("unchecked")
    public static final ThreadLocal<Map<String, Object>> safeProperties = new ThreadLocal();

    // 非线程本地变量
    public static final Map<String, Object> unsafeProperties = new ConcurrentHashMap<>();
}
