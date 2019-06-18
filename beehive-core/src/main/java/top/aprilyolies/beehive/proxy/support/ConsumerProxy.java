package top.aprilyolies.beehive.proxy.support;

import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.ReflectUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author EvaJohnson
 * @Date 2019-06-18
 * @Email g863821569@gmail.com
 */
public abstract class ConsumerProxy implements Proxy {
    private static final Map<ClassLoader, Map<String, Object>> PROXY_CACHE_MAP = new WeakHashMap<>();

    private static final Object PENDING_GENERATION_MARKER = new Object();

    private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);


    public static final InvocationHandler THROW_UNSUPPORTED_INVOKER = (proxy, method, args) -> {
        throw new UnsupportedOperationException("Method [" + ReflectUtils.getName(method) + "] unimplemented.");
    };

    /**
     * Get proxy.这里获取的 proxy 是 Proxy0，只有一个 newInstance 方法，用来构建 proxy0
     *
     * @param clazz interface class array.
     * @return Proxy instance.
     */
    public static ConsumerProxy getProxy(Class<?>... clazz) {
        return getProxy(ClassUtils.getClassLoader(ConsumerProxy.class), clazz);
    }

    /**
     * Get proxy.
     *
     * @param cl  class loader.
     * @param ics interface class array.
     * @return Proxy instance.
     */
    public static ConsumerProxy getProxy(ClassLoader cl, Class<?>... ics) {
        StringBuilder sb = new StringBuilder();
        // 这个 for 循环就是根据 ics 构建缓存的 key
        for (int i = 0; i < ics.length; i++) {  // 这里拼接的就是 ics 每一项的全限定名
            String itf = ics[i].getName();
            if (!ics[i].isInterface()) {
                throw new RuntimeException(itf + " is not a interface.");
            }

            Class<?> tmp = null;
            try {
                tmp = Class.forName(itf, false, cl);
            } catch (ClassNotFoundException e) {
            }

            if (tmp != ics[i]) {
                throw new IllegalArgumentException(ics[i] + " is not visible from class loader");
            }
            // 这里拼接的就是 ics 每一项的全限定名
            sb.append(itf).append(';');
        }
        // org.apache.dubbo.demo.DemoService;com.alibaba.dubbo.rpc.service.EchoService;
        // use interface class name list as key.
        String key = sb.toString();

        // get cache by class loader.
        Map<String, Object> cache;
        synchronized (PROXY_CACHE_MAP) {
            cache = PROXY_CACHE_MAP.computeIfAbsent(cl, k -> new HashMap<>());
        }
        // 这里就是一个 cl 对应一个 map
        ConsumerProxy proxy = null;
        synchronized (cache) {
            do {
                Object value = cache.get(key);  // 这里是尝试从缓存中获取
                // PENDING_GENERATION_MARKER，表明构建过程中，先 wait 一下，避免重复构建
                if (value == PENDING_GENERATION_MARKER) {
                    try {
                        cache.wait();
                    } catch (InterruptedException e) {
                    }
                } else {
                    cache.put(key, PENDING_GENERATION_MARKER);  // 投放标志对象
                    break;
                }
            }
            while (true);
        }

        long id = PROXY_CLASS_COUNTER.getAndIncrement();
        String pkg = null;
        ClassGenerator ccp = null, ccm = null;
        try {
            ccp = ClassGenerator.newInstance(cl);   // 又要用到 ClassGenerator 了，底层有 Javassist 实现s

            Set<String> worked = new HashSet<>();
            List<Method> methods = new ArrayList<>();

            for (int i = 0; i < ics.length; i++) {
                if (!Modifier.isPublic(ics[i].getModifiers())) {    // 非 public 修饰的类，处理方式
                    String npkg = ics[i].getPackage().getName();
                    if (pkg == null) {
                        pkg = npkg; // 获取该 ics 所在的包名
                    } else {
                        if (!pkg.equals(npkg)) {
                            throw new IllegalArgumentException("non-public interfaces from different packages");
                        }
                    }
                }
                ccp.addInterface(ics[i]);   // 指定接口

                for (Method method : ics[i].getMethods()) {
                    String desc = ReflectUtils.getDesc(method); // 方法的签名
                    if (worked.contains(desc)) {
                        continue;
                    }
                    worked.add(desc);   // 记录已经处理过的方法签名

                    int ix = methods.size();
                    Class<?> rt = method.getReturnType();   // 方法的返回类型
                    Class<?>[] pts = method.getParameterTypes();    // 方法的参数类型
                    // Object[] args = new Object[1];
                    StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
                    for (int j = 0; j < pts.length; j++) {  // Object[] args = new Object[1]; args[0] = ($w)$1;
                        code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");
                    }   // Object[] args = new Object[1]; args[0] = ($w)$1; Object ret = handler.invoke(this, methods[0], args);
                    code.append(" Object ret = handler.invoke(this, methods[").append(ix).append("], args);");
                    if (!Void.TYPE.equals(rt)) {    // Object[] args = new Object[1]; args[0] = ($w)$1; Object ret = handler.invoke(this, methods[0], args); return (java.lang.String)ret;
                        code.append(" return ").append(asArgument(rt, "ret")).append(";");
                    }

                    methods.add(method);    // Object[] args = new Object[1]; args[0] = ($w)$1; Object ret = handler.invoke(this, methods[0], args); return (java.lang.String)ret;
                    ccp.addMethod(method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
                }
            }

            if (pkg == null) {
                pkg = ConsumerProxy.class.getPackage().getName(); // org.apache.dubbo.common.bytecode
            }

            // create ProxyInstance class.
            String pcn = pkg + ".proxy" + id;   // 代理类名字 org.apache.dubbo.common.bytecode.proxy0
            ccp.setClassName(pcn);
            ccp.addField("public static java.lang.reflect.Method[] methods;");
            ccp.addField("private " + InvocationHandler.class.getName() + " handler;");
            ccp.addConstructor(Modifier.PUBLIC, new Class<?>[]{InvocationHandler.class}, new Class<?>[0], "handler=$1;");
            ccp.addDefaultConstructor();
            Class<?> clazz = ccp.toClass();
            clazz.getField("methods").set(null, methods.toArray(new Method[0]));

            // create Proxy class.
            String fcn = ConsumerProxy.class.getName() + id;
            ccm = ClassGenerator.newInstance(cl);
            ccm.setClassName(fcn);
            ccm.addDefaultConstructor();
            ccm.setSuperClass(ConsumerProxy.class);
            ccm.addMethod("public Object newInstance(" + InvocationHandler.class.getName() + " h){ return new " + pcn + "($1); }");
            // public class org.apache.dubbo.common.bytecode.Proxy0 extends org.apache.dubbo.common.bytecode.Proxy {
            //     public Object newInstance(java.lang.reflect.InvocationHandler h) {
            //         return new org.apache.dubbo.common.bytecode.proxy0($1);
            //     }
            // }
            Class<?> pc = ccm.toClass();
            proxy = (ConsumerProxy) pc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            // release ClassGenerator
            if (ccp != null) {
                ccp.release();
            }
            if (ccm != null) {
                ccm.release();
            }
            synchronized (cache) {
                if (proxy == null) {
                    cache.remove(key);
                } else {
                    cache.put(key, new WeakReference<ConsumerProxy>(proxy));
                }
                cache.notifyAll();
            }
        }
        return proxy;
    }

    private static String asArgument(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (Boolean.TYPE == cl) {
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            }
            if (Byte.TYPE == cl) {
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            }
            if (Character.TYPE == cl) {
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            }
            if (Double.TYPE == cl) {
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            }
            if (Float.TYPE == cl) {
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            }
            if (Integer.TYPE == cl) {
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            }
            if (Long.TYPE == cl) {
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            }
            if (Short.TYPE == cl) {
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            }
            throw new RuntimeException(name + " is unknown primitive type.");
        }
        return "(" + ReflectUtils.getName(cl) + ")" + name;
    }

    /**
     * get instance with default handler.
     *
     * @return instance.
     */
    public Object newInstance() {
        return newInstance(THROW_UNSUPPORTED_INVOKER);
    }

    /**
     * get instance with special handler.
     *
     * @return instance.
     */
    abstract public Object newInstance(InvocationHandler handler);
}
