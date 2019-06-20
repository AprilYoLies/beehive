package top.aprilyolies.beehive.invoker;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.cluster.loadbalance.LoadBalance;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.result.RpcResult;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.filter.AccessLogFilter;
import top.aprilyolies.beehive.filter.Filter;
import top.aprilyolies.beehive.filter.MonitorFilter;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public abstract class AbstractInvoker<T> implements Invoker {
    protected final Logger logger = Logger.getLogger(getClass());

    protected LoadBalance loadBalanceSelector = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtensionSelectorInstance();

    @Override
    public Object invoke(InvokeInfo info) {
        try {
            return doInvoke(info);
        } catch (Exception e) {
            e.printStackTrace();
            return new RpcResult();
        }
    }

    protected abstract Object doInvoke(InvokeInfo info);

    /**
     * 通过 filter 构建 invoker 链，最后一个 invoker 就是我们创建的 ProxyWrapperInvoker，它封装了我们真正的调用逻辑
     *
     * @param invoker 原始的 invoker
     * @return 通过 filter 构建出来的 invoker 链
     */
    public static Invoker buildInvokerChain(Invoker<?> invoker) {
        // TODO 这里的 filter 获取应该通过 ExtensionLoader
        List<Filter> filters = new ArrayList<>();
        filters.add(new AccessLogFilter());
        filters.add(new MonitorFilter());
        Invoker ptr = invoker;
        if (filters.size() > 0) {
            for (Filter filter : filters) {
                final Invoker next = ptr;
                Invoker pre = new AbstractInvoker() {
                    @Override
                    protected Object doInvoke(InvokeInfo info) {
                        return filter.doFilter(next, info);
                    }
                };
                ptr = pre;
            }
        }
        //noinspection unchecked
        return ptr;
    }

}
