package top.aprilyolies.beehive.filter;

import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.result.Result;
import top.aprilyolies.beehive.invoker.Invoker;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public interface Filter {
    Result doFilter(Invoker next, InvokeInfo invokeInfo);
}
