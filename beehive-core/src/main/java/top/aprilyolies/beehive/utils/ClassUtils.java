package top.aprilyolies.beehive.utils;

import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */
public class ClassUtils {
    private static Logger logger = Logger.getLogger(ClassUtils.class);

    /**
     * get class loader
     *
     * @param clazz
     * @return class loader
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = null;
        try {
            // 返回 Thread 的 contextClassLoader
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            logger.info("Cannot access thread context ClassLoader - falling back to system class loader...");
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            // 返回 class 的类加载器，如果是 bootstrap 类加载器，将会返回 null
            // class 实例中会有一个 classLoader 属性
            cl = clazz.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }

        return cl;
    }

    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName() + ": ");
        if (e.getMessage() != null) {
            p.print(e.getMessage() + "\n");
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    public static ClassLoader getCallerClassLoader(Class<?> caller) {
        return caller.getClassLoader();
    }

    public static Class<?> forName(String[] packages, String className) {
        try {
            return classForName(className);
        } catch (ClassNotFoundException e) {
            if (packages != null && packages.length > 0) {
                for (String pkg : packages) {
                    try {
                        return classForName(pkg + "." + className);
                    } catch (ClassNotFoundException e2) {
                    }
                }
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> forName(String className) {
        try {
            return classForName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> classForName(String className) throws ClassNotFoundException {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean[]":
                return boolean[].class;
            case "byte[]":
                return byte[].class;
            case "char[]":
                return char[].class;
            case "short[]":
                return short[].class;
            case "int[]":
                return int[].class;
            case "long[]":
                return long[].class;
            case "float[]":
                return float[].class;
            case "double[]":
                return double[].class;
            default:
        }
        try {
            return arrayForName(className);
        } catch (ClassNotFoundException e) {
            // try to load from java.lang package
            if (className.indexOf('.') == -1) {
                try {
                    return arrayForName("java.lang." + className);
                } catch (ClassNotFoundException e2) {
                    // ignore, let the original exception be thrown
                }
            }
            throw e;
        }
    }

    private static Class<?> arrayForName(String className) throws ClassNotFoundException {
        return Class.forName(className.endsWith("[]")
                ? "[L" + className.substring(0, className.length() - 2) + ";"
                : className, true, Thread.currentThread().getContextClassLoader());
    }

    /**
     * get simple class name from qualified class name
     */
    public static String getSimpleClassName(String qualifiedName) {
        if (null == qualifiedName) {
            return null;
        }

        int i = qualifiedName.lastIndexOf('.');
        return i < 0 ? qualifiedName : qualifiedName.substring(i + 1);
    }

    public static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return isPrimitive(cls.getComponentType());
        }
        return isPrimitive(cls);
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    // 用于判断方法是否是 setter 方法（set 开头，参数个数为 1，修饰符为 public）
    public static boolean isSetterMethod(Method method) {
        return method.getModifiers() == Modifier.PUBLIC && method.getParameters().length == 1 && method.getName().startsWith("set");
    }

    // 用于判断方法是否是 getter 方法（get 开头，参数个数为 0，修饰符为 public）
    public static boolean isGetterrMethod(Method method) {
        return method.getModifiers() == Modifier.PUBLIC && method.getParameters().length == 0 && method.getName().startsWith("get");
    }

    /**
     * 根据 getter 或者 setter 方法获取对应的属性值
     *
     * @param method 方法类
     * @return 方法对应的属性名
     */
    public static String method2Property(Method method) {
        String name = method.getName();
        if (!name.startsWith("get") && !name.startsWith("set") && name.length() <= 3)
            return "";
        return name.substring(3, 4).toLowerCase() + name.substring(4);
    }
}