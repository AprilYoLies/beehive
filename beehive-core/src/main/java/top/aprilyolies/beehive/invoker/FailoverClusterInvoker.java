package top.aprilyolies.beehive.invoker;

import top.aprilyolies.beehive.cluster.loadbalance.LoadBalance;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.transporter.client.Client;

import java.net.URLDecoder;
import java.util.ArrayList;
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
        Invoker<T> invoker = selectInvoker(loadBalance, invokers);
        Invoker chain = buildInvokerChain(invoker);
        return chain.invoke(info);
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
        //noinspection unchecked
        List<String> providers = BeehiveContext.safeGet(UrlConstants.PROVIDERS, List.class);
        Client server = BeehiveContext.safeGet(UrlConstants.CONSUMERS_TRANSPORT, Client.class);
        assert providers != null;
        return createRemoteInvoker(providers, server);
    }

    private List<Invoker<T>> createRemoteInvoker(List<String> providers, Client client) {
        List<Invoker<T>> invokers = new ArrayList<>(providers.size());
        for (String provider : providers) {
            String s = URLDecoder.decode(provider);
            URL url = URL.buildFromAddress(s);
            String host = url.getHost();
            int port = url.getPort();
            RemoteInvoker invoker = new RemoteInvoker(host, port, client);
            invokers.add(invoker);
        }
        return invokers;
    }
}
