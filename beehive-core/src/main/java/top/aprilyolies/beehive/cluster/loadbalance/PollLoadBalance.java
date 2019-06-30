package top.aprilyolies.beehive.cluster.loadbalance;

import top.aprilyolies.beehive.extension.annotation.Prototype;
import top.aprilyolies.beehive.invoker.Invoker;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-29
 * @Email g863821569@gmail.com
 */
@Prototype
public class PollLoadBalance extends AbstractLoadBalance {
    private int idx = 0;

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers) {
        int size = invokers.size();
        if (idx >= size) {
            idx = 0;
        }
        return invokers.get(idx++);
    }
}
