package top.aprilyolies.beehive.extension;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.extension.annotation.Selector;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Author EvaJohnson
 * @Date 2019-06-11
 * @Email g863821569@gmail.com
 */
public class CodeGenerator {
    Logger logger = Logger.getLogger(CodeGenerator.class);

    private Class<?> type;

    public CodeGenerator(Class<?> type) {
        this.type = type;
    }

    public String generateCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPackageInfo());
        sb.append(getImportInfo());
        sb.append(getClassInfo());
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            sb.append(getMethodInfo(method));
        }
        sb.append("}");
        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }
        return sb.toString();
    }

    private String getMethodInfo(Method method) {
        String returnType = method.getReturnType().getCanonicalName();
        String methodName = method.getName();
        String methodContent = getMethodContent(method);
        String methodArgs = getMethodArgsInfo(method);
        String exceptionInfo = getExceptionInfo(method);
        return String.format("public %s %s(%s) %s {\n%s}\n", returnType, methodName, methodArgs, exceptionInfo, methodContent);
    }

    // 获取抛出的异常串
    private String getExceptionInfo(Method method) {
        Class<?>[] ets = method.getExceptionTypes();
        String list = "";
        if (ets.length > 0) {
            list = Arrays.stream(ets).map(Class::getCanonicalName).collect(Collectors.joining(", "));
            return String.format("throws %s", list);
        }
        return list;
    }

    // 拼凑方法参数串
    private String getMethodArgsInfo(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return IntStream.range(0, pts.length)
                .mapToObj(i -> String.format("%s arg%d", pts[i].getCanonicalName(), i))
                .collect(Collectors.joining(", "));
    }

    // 生成方法的内容信息
    private String getMethodContent(Method method) {
        Selector anno = method.getAnnotation(Selector.class);
        StringBuffer sb = new StringBuffer();
        if (anno == null) {
            sb.append(getUnsupportedInfo(method));
        } else {
            int urlParaIndex = getUrlParaIndex(method);
            if (urlParaIndex != -1) {
                sb.append(getUrlCheck(urlParaIndex));
                sb.append(getExtensionNameInfo());
                sb.append(getExtensionNameCheck());
                sb.append(getExtensionAssignment());
                sb.append(getReturnInfo(method));
            } else {
                sb.append(getUnsupportedInfo(method));
            }
        }
        return sb.toString();
    }

    private String getReturnInfo(Method method) {
        String returnStatement = method.getReturnType().equals(void.class) ? "" : "return ";
        // 获取方法的参数，用 "，" 进行分割
        String args = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.joining(", "));
        // 拼接方法返回代码，调用 extension 的 method 方法
        return returnStatement + String.format("extension.%s(%s);\n", method.getName(), args);
    }

    private String getExtensionAssignment() {
        return String.format("%s extension = (%s)ExtensionLoader.getExtensionLoader(%s.class).getExtension(extName);\n", type.getName(),
                type.getName(), type.getName());
    }

    private String getExtensionNameCheck() {
        return "if (extName == null) throw new IllegalStateException(\"The extension name got from url should not be empty.\");\n";
    }

    private String getExtensionNameInfo() {
        return "String extName = (url.getProtocol() == null ? \"dubbo\" : url.getProtocol());\n";
    }

    private String getUrlCheck(int urlParaIndex) {
        return String.format("if (arg%d == null) throw new IllegalArgumentException(\"Parameter url should not be null.\");\n%s " +
                "url = arg%d;\n", urlParaIndex, top.aprilyolies.beehive.common.URL.class.getName(), urlParaIndex);
    }

    // 找到 Selector 注解的方法 URL 参数的位置索引
    private int getUrlParaIndex(Method method) {
        int urlParaIndex = -1;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == top.aprilyolies.beehive.common.URL.class) {
                urlParaIndex = i;
                break;
            }
        }
        return urlParaIndex;
    }

    private String getUnsupportedInfo(Method method) {
        return String.format("throw new UnsupportedOperationException(\"Method %s of interface %s must annotated with " +
                        "%s and has a %s parameter.\");\n", method.getName(), type.getCanonicalName(), Selector.class.getName(),
                top.aprilyolies.beehive.common.URL.class.getName());
    }

    private String getClassInfo() {
        // 对于数组或者内部类 getCanonicalName 获取的名字才是我们所理解的名字
        return String.format("public class %s$Selector implements %s {", type.getSimpleName(), type.getCanonicalName());
    }

    private String getImportInfo() {
        return String.format("import %s;\n", ExtensionLoader.class.getName());
    }

    private String getPackageInfo() {
        return String.format("%s;\n", type.getPackage());
    }
}
