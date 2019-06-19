package top.aprilyolies.beehive.cluster;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.invoker.Invoker;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */

/**
 * 该接口用于表示模拟的集群，实现类需要模拟相应集群的实现，比如 failover（容错型），fast-failed（快速失败型）
 */
@SPI("failover")
public interface Cluster {
    /**
     * 该方法应该根据集群的特性返回一个 Invoker，通常这个 Invoker 是根据 provider 信息来构建的
     *
     * @param <T>
     * @return
     */
    <T> Invoker<T> join(URL url);
}
