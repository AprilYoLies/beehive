package top.aprilyolies.beehive.cluster.loadbalance;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.invoker.Invoker;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    protected final Logger logger = Logger.getLogger(getClass());

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers) {
        throw new UnsupportedOperationException("Only load balance instance could call this method");
    }

    @Override
    public LoadBalance createLoadBalance(URL url) {
        throw new UnsupportedOperationException("Only load balance selector could call this method");
    }
}
