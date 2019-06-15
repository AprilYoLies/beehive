package top.aprilyolies.beehive.invoker;

import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.result.Result;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */

/**
 * 该接口的实现类是 invoker 调用链的组成部分，目的是为了使得调用更加灵活或者方便拓展，比如统计调用的次数、单次服务调用的时间
 *
 * @param <T>
 */
public interface Invoker<T> {
    /**
     * 调用链的调用传递方法，为了保证调用的顺利传递，一定要记得执行下一个 invoker 的 invoke 方法
     *
     * @param info 这是 invoke 方法执行所需要的一些相关参数
     * @return
     */
    Result invoke(InvokeInfo info);
}
