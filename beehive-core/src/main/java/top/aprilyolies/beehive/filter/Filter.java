package top.aprilyolies.beehive.filter;

import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.result.Result;
import top.aprilyolies.beehive.invoker.Invoker;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */

/**
 * 过滤器接口，实现类需要实现相应的过滤功能，借助于 ExtensionLoader 会使得拓展更加灵活
 */
public interface Filter {
    /**
     * 实现类进行过滤的方法，在执行完逻辑后，一定要记得执行 next.invoke() 方法
     *
     * @param next       invoker 链中，当前 invoker 的下一个 invoker
     * @param invokeInfo 执行的 invoker 的一些相关的参数
     * @return
     */
    Result doFilter(Invoker next, InvokeInfo invokeInfo);
}
