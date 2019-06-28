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
    private static final Random random = new Random(System.currentTimeMillis());

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers) {
        try {
            if (invokers == null || invokers.size() == 0) {
                return null;
            }
            int idx = Math.abs(random.nextInt() % invokers.size());
            if (logger.isDebugEnabled()) {
                logger.debug("There are " + invokers.size() + " invokers, RandomLoadBalance choose the invoker with" +
                        "index of " + idx);
            }
            return invokers.get(idx);
        } catch (Exception e) {
            logger.error("Got invoker failed, this may caused by some new provider was added, and the beehive" +
                    " was refresh the invokers list");
            return null;
        }
    }

}
