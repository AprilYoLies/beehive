package top.aprilyolies.beehive.proxy;

import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.invoker.Invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public class InvokerInvocationHandler implements InvocationHandler {
    private final URL url;
    private Invoker invoker;

    public InvokerInvocationHandler(Invoker invoker, URL url) {
        this.invoker = invoker;
        this.url = url;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);    // 如果是调用的 Object 类的方法，那么直接调用 invoker 的此方法
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        // 除开上述几种方法，那么就直接调用 invoker 的 invoke 方法，reCreate 是对返回结果的再处理
        return invoker.invoke(createInvocation(method, args));
    }

    private InvokeInfo createInvocation(Method method, Object[] args) {  // 这里创建出来的就是 RpcInvocation
        return new InvokeInfo(method.getName(),
                method.getParameterTypes(),
                args,
                null,
                url.getParameter(UrlConstants.SERVICE));
    }
}
