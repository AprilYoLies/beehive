package top.aprilyolies.beehive.cluster.loadbalance;

import top.aprilyolies.beehive.invoker.Invoker;

import java.util.List;
import java.util.Random;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    private static final Random random = new Random(7440);

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers) {
        return invokers.get(Math.abs(random.nextInt() % invokers.size()));
    }

}
