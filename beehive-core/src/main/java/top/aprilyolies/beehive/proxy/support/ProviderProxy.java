package top.aprilyolies.beehive.proxy.support;

import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.spring.ServiceConfigBean;
import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.ReflectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public abstract class ProviderProxy implements Proxy {
    private static final Map<Class<?>, ProviderProxy> proxyCache = new ConcurrentHashMap<>();
    private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);

    public static ProviderProxy getProxy(Class<?> clazz) {
        if (proxyCache.get(clazz) != null)
            return proxyCache.get(clazz);
        proxyCache.computeIfAbsent(clazz, t -> createProxy(clazz));
        return proxyCache.get(clazz);
    }

    private static ProviderProxy createProxy(Class<?> clazz) {
        // 不能对 9 中基本类型的包装类型构建 ProviderProxy
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("Can not create ProviderProxy for primitive type: " + clazz);
        }

        String name = clazz.getName();
        ClassLoader cl = ClassUtils.getClassLoader(clazz);

        // invokeMethod
        // public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws java.lang.reflect.InvocationTargetException{
        StringBuilder sb = new StringBuilder("public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws " + InvocationTargetException.class.getName() + ", java.lang.NoSuchMethodException { ");

        sb.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");

        Method[] methods = clazz.getMethods();
        // get all public method.
        // methods 方法不为空，且不全为 Object 声明的方法
        boolean hasMethod = hasMethods(methods);
        if (hasMethod) {
            // 通过 methods 对 c3 进行构造
            sb.append(" try{");
            for (Method m : methods) {
                //ignore Object's method.
                if (m.getDeclaringClass() == Object.class) {
                    continue;
                }

                // 通过方法名进行构造
                String mn = m.getName();
                sb.append(" if( \"").append(mn).append("\".equals( $2 ) ");
                int len = m.getParameterTypes().length;
                // 补上方法参数信息
                sb.append(" && ").append(" $3.length == ").append(len);

                boolean override = false;
                for (Method m2 : methods) {
                    // 如果有跟当前方法同名的其它方法，即存在重写（overwrite）
                    if (m != m2 && m.getName().equals(m2.getName())) {
                        override = true;
                        break;
                    }
                }
                if (override) {
                    if (len > 0) {
                        for (int l = 0; l < len; l++) {
                            // 补上方法参数信息
                            sb.append(" && ").append(" $3[").append(l).append("].getName().equals(\"")
                                    .append(m.getParameterTypes()[l].getName()).append("\")");
                        }
                    }
                }
                sb.append(" ) { ");

                // 拼接方法的返回值
                if (m.getReturnType() == Void.TYPE) {
                    sb.append(" w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");").append(" return null;");
                } else {
                    sb.append(" return ($w)w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");");
                }

                // 拼接完返回值后
                sb.append(" }");
            }
            sb.append(" } catch(Throwable e) { ");
            sb.append("     throw new java.lang.reflect.InvocationTargetException(e); ");
            sb.append(" }");
        }

        // 补全括号即剩余信息
        sb.append(" throw new " + NoSuchMethodException.class.getName() + "(\"Not found method \\\"\"+$2+\"\\\" in class " + clazz.getName() + ".\"); }");

        // make class
        // 对构建的 ProviderProxy 类进行计数
        long id = PROXY_CLASS_COUNTER.getAndIncrement();
        // 根据 classLoader 构建 ClassGenerator
        ClassGenerator cg = ClassGenerator.newInstance(cl);
        // 补全一些类信息
        cg.setClassName((Modifier.isPublic(clazz.getModifiers()) ? ProviderProxy.class.getName() : clazz.getName() + "$sw") + id);
        cg.setSuperClass(ProviderProxy.class);

        cg.addDefaultConstructor();

        cg.addMethod(sb.toString());

        try {
            // 根据 ClassGenerator 生成真正的 Class 对象
            Class<?> wc = cg.toClass();
            // setup static field.
            // 根据上边获取的信息对 Class 对象的某些属性进行填充
            return (ProviderProxy) wc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            // 对相关资源进行释放
            cg.release();
        }
    }

    // methods 方法不为空，且不全为 Object 声明的方法
    private static boolean hasMethods(Method[] methods) {
        if (methods == null || methods.length == 0) {
            return false;
        }
        for (Method m : methods) {
            // 方法不能全部是 Object 类所声明
            if (m.getDeclaringClass() != Object.class) {
                return true;
            }
        }
        return false;
    }

    private static String args(Class<?>[] cs, String name) {
        int len = cs.length;
        if (len == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(arg(cs[i], name + "[" + i + "]"));
        }
        return sb.toString();
    }

    private static String arg(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (cl == Boolean.TYPE) {
                return "((Boolean)" + name + ").booleanValue()";
            }
            if (cl == Byte.TYPE) {
                return "((Byte)" + name + ").byteValue()";
            }
            if (cl == Character.TYPE) {
                return "((Character)" + name + ").charValue()";
            }
            if (cl == Double.TYPE) {
                return "((Number)" + name + ").doubleValue()";
            }
            if (cl == Float.TYPE) {
                return "((Number)" + name + ").floatValue()";
            }
            if (cl == Integer.TYPE) {
                return "((Number)" + name + ").intValue()";
            }
            if (cl == Long.TYPE) {
                return "((Number)" + name + ").longValue()";
            }
            if (cl == Short.TYPE) {
                return "((Number)" + name + ").shortValue()";
            }
            throw new RuntimeException("Unknown primitive type: " + cl.getName());
        }
        return "(" + ReflectUtils.getName(cl) + ")" + name;
    }

    /**
     * 创建 jdk 代理类
     *
     * @param classes
     * @return
     */
    public static Proxy getJdkProxy(Class<?>... classes) {
        ClassLoader classLoader = classes[0].getClassLoader();
        // 根据 url 信息获取 invoke target 实例
        ServiceConfigBean serviceConfigBean = BeehiveContext.unsafeGet(UrlConstants.PROVIDER_MODEL, ServiceConfigBean.class);
        Object target = serviceConfigBean.getRef();
        ProviderInvocationHandler handler = new ProviderInvocationHandler(target);
        return (Proxy) java.lang.reflect.Proxy.newProxyInstance(classLoader, classes, handler);
    }

    /**
     * provider 的 jdk invocation handler
     */
    private static class ProviderInvocationHandler implements InvocationHandler {

        private final Object target;

        public ProviderInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(target, args);
        }
    }

    /**
     * invoke method.
     *
     * @param instance instance.
     * @param mn       method name.
     * @param types
     * @param args     argument array.
     * @return return value.
     */
    abstract public Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args) throws
            NoSuchMethodException, InvocationTargetException;
}
