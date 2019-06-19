package top.aprilyolies.beehive.cluster;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.invoker.FailoverClusterInvoker;
import top.aprilyolies.beehive.invoker.Invoker;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public class FailoverCluster implements Cluster {
    @Override
    public <T> Invoker<T> join(URL url) {
        //noinspection unchecked
        return new FailoverClusterInvoker(url);
    }
}
