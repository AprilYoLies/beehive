package top.aprilyolies.beehive.invoker;

import top.aprilyolies.beehive.cluster.loadbalance.LoadBalance;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.URL;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public class FailoverClusterInvoker<T> extends AbstractInvoker {
    private final URL url;

    public FailoverClusterInvoker(URL url) {
        this.url = url;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) {
        List<Invoker<T>> invokers = listInvokers();
        LoadBalance loadBalance = createLoadBalance(url);

        return selectInvoker(loadBalance, invokers);
    }

    private Invoker<T> selectInvoker(LoadBalance loadBalance, List<Invoker<T>> invokers) {
        if (invokers.isEmpty()) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return loadBalance.select(invokers);
    }

    private LoadBalance createLoadBalance(URL url) {
        return loadBalanceSelector.createLoadBalance(url);
    }

    private List<Invoker<T>> listInvokers() {
        return null;
    }
}
