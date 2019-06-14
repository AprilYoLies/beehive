package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.proxy.support.ClassGenerator;
import top.aprilyolies.beehive.utils.ClassUtils;
import top.aprilyolies.beehive.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public abstract class Proxy {
    private static final Map<Class<?>, Proxy> proxyCache = new ConcurrentHashMap<>();
    private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);

    public static Proxy getProxy(Class<?> clazz) {
        if (proxyCache.get(clazz) != null)
            return proxyCache.get(clazz);
        proxyCache.computeIfAbsent(clazz, t -> createProxy(clazz));
        return proxyCache.get(clazz);
    }

    private static Proxy createProxy(Class<?> clazz) {
        // 不能对 9 中基本类型的包装类型构建 Proxy
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("Can not create Proxy for primitive type: " + clazz);
        }

        String name = clazz.getName();
        ClassLoader cl = ClassUtils.getClassLoader(clazz);

        // invokeMethod
        // public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws java.lang.reflect.InvocationTargetException{
        StringBuilder c3 = new StringBuilder("public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws " + InvocationTargetException.class.getName() + ", java.lang.NoSuchMethodException { ");

        c3.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");

        // 保存字段的 name 和 type
        Map<String, Class<?>> pts = new HashMap<>(); // <property name, property types>
        // 保存方法的 desc（签名） 和 instance
        // get method desc.
        // int do(int arg1) => "do(I)I"
        // void do(String arg1,boolean arg2) => "do(Ljava/lang/String;Z)V"
        Map<String, Method> ms = new LinkedHashMap<>(); // <method desc, Method instance>
        // 保存方法名，包括 Object 类声明的方法
        List<String> mns = new ArrayList<>(); // method names.
        // 保存方法名，仅限服务接口声明的方法
        List<String> dmns = new ArrayList<>(); // declaring method names.

        // get all public field.
        for (Field f : clazz.getFields()) {
            String fn = f.getName();
            Class<?> ft = f.getType();
            // 避开 static 和 transient 修饰的字段
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                continue;
            }

            // 保存字段的 name 和 type
            pts.put(fn, ft);
        }

        Method[] methods = clazz.getMethods();
        // get all public method.
        // methods 方法不为空，且不全为 Object 声明的方法
        boolean hasMethod = hasMethods(methods);
        if (hasMethod) {
            // 通过 methods 对 c3 进行构造
            c3.append(" try{");
            for (Method m : methods) {
                //ignore Object's method.
                if (m.getDeclaringClass() == Object.class) {
                    continue;
                }

                // 通过方法名进行构造
                String mn = m.getName();
                c3.append(" if( \"").append(mn).append("\".equals( $2 ) ");
                int len = m.getParameterTypes().length;
                // 补上方法参数信息
                c3.append(" && ").append(" $3.length == ").append(len);

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
                            c3.append(" && ").append(" $3[").append(l).append("].getName().equals(\"")
                                    .append(m.getParameterTypes()[l].getName()).append("\")");
                        }
                    }
                }
                c3.append(" ) { ");

                // 拼接方法的返回值
                if (m.getReturnType() == Void.TYPE) {
                    c3.append(" w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");").append(" return null;");
                } else {
                    c3.append(" return ($w)w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");");
                }

                // 拼接完返回值后
                c3.append(" }");

                // 保存拼接的方法名
                mns.add(mn);
                if (m.getDeclaringClass() == clazz) {
                    // 保存方法名，仅限服务接口声明的方法
                    dmns.add(mn);
                }
                // 保存方法的签名和方法实例
                ms.put(ReflectUtils.getDesc(m), m);
            }
            c3.append(" } catch(Throwable e) { ");
            c3.append("     throw new java.lang.reflect.InvocationTargetException(e); ");
            c3.append(" }");
        }

        // 补全括号即剩余信息
        c3.append(" throw new " + NoSuchMethodException.class.getName() + "(\"Not found method \\\"\"+$2+\"\\\" in class " + clazz.getName() + ".\"); }");

        // make class
        // 对构建的 Proxy 类进行计数
        long id = PROXY_CLASS_COUNTER.getAndIncrement();
        // 根据 classLoader 构建 ClassGenerator
        ClassGenerator cc = ClassGenerator.newInstance(cl);
        // 补全一些类信息
        cc.setClassName((Modifier.isPublic(clazz.getModifiers()) ? Proxy.class.getName() : clazz.getName() + "$sw") + id);
        cc.setSuperClass(Proxy.class);

        cc.addDefaultConstructor();
        cc.addField("public static String[] pns;"); // property name array.
        cc.addField("public static " + Map.class.getName() + " pts;"); // property type map.
        cc.addField("public static String[] mns;"); // all method name array.
        cc.addField("public static String[] dmns;"); // declared method name array.
        for (int i = 0, len = ms.size(); i < len; i++) {    // 方法的参数类型数组，有几个函数就会有几个数组
            cc.addField("public static Class[] mts" + i + ";");
        }

        cc.addMethod("public String[] getPropertyNames(){ return pns; }");
        cc.addMethod("public boolean hasProperty(String n){ return pts.containsKey($1); }");
        cc.addMethod("public Class getPropertyType(String n){ return (Class)pts.get($1); }");
        cc.addMethod("public String[] getMethodNames(){ return mns; }");
        cc.addMethod("public String[] getDeclaredMethodNames(){ return dmns; }");
        cc.addMethod(c3.toString());

        try {
            // 根据 ClassGenerator 生成真正的 Class 对象
            Class<?> wc = cc.toClass();
            // setup static field.
            // 根据上边获取的信息对 Class 对象的某些属性进行填充
            // 字段的 name 和 type
            wc.getField("pts").set(null, pts);
            // pns 集合专门用来保存字段的名字
            wc.getField("pns").set(null, pts.keySet().toArray(new String[0]));
            // mns 集合专门用来保存方法名
            wc.getField("mns").set(null, mns.toArray(new String[0]));
            // 保存方法名，仅限服务接口声明的方法
            wc.getField("dmns").set(null, dmns.toArray(new String[0]));
            int ix = 0;
            // ms 保存的是方法签名和方法实例
            // 遍历方法实例
            for (Method m : ms.values()) {
                // mts 字段用来保存方法的参数类型
                wc.getField("mts" + ix++).set(null, m.getParameterTypes());
            }
            // 返回构建的 Proxy 实例
            return (Proxy) wc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            // 对相关资源进行释放
            cc.release();
            ms.clear();
            mns.clear();
            dmns.clear();
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
