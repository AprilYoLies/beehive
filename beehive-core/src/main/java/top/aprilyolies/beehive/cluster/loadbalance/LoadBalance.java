package top.aprilyolies.beehive.cluster.loadbalance;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.invoker.Invoker;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */

/**
 * 该接口的实现类适用于进行负载均衡的，也就是能够按照一定的逻辑，从 invokers 中选择一个进行返回
 */
@SPI("random")
public interface LoadBalance {
    /**
     * 进行 invoker 的选择，从 invokers 中选择一个返回，不同的实现类，选择的逻辑不一样
     *
     * @param invokers 这个 invokers 是根据 provider 信息构建出来的 invoker 集合
     * @param <T>
     * @return 经过负载均衡返回的 invoker
     */
    <T> Invoker<T> select(List<Invoker<T>> invokers);

    /**
     * 根据 url 信息获取 LoadBalance
     *
     * @param url
     * @return url 指定的负载均衡器
     */
    LoadBalance createLoadBalance(URL url);
}
