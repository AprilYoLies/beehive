package top.aprilyolies.beehive.invoker;

import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.InvokeInfo;
import top.aprilyolies.beehive.common.result.Result;
import top.aprilyolies.beehive.common.result.RpcResult;

import java.lang.reflect.InvocationTargetException;


/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public abstract class AbstractInvoker<T> implements Invoker {
    protected final Logger logger = Logger.getLogger(getClass());

    @Override
    public Result invoke(InvokeInfo info) {
        try {
            Object msg = doInvoke(info);
            new RpcResult(msg);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return new RpcResult();
        }
    }

    protected abstract Object doInvoke(InvokeInfo info) throws NoSuchMethodException, InvocationTargetException;

}
