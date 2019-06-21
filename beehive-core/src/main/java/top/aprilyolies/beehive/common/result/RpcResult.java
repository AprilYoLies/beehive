package top.aprilyolies.beehive.common.result;

import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class RpcResult implements Result {
    private static final Logger logger = Logger.getLogger(RpcResult.class);
    // rpc 的
    private Object msg;
    // 可重入锁
    ReentrantLock lock = new ReentrantLock();
    // 结果完成的条件
    Condition finishCondition = lock.newCondition();
    // 获取结果的超时时间
    private int timeout = 3000;
    // 结果完成标志
    private volatile boolean finished = false;

    public RpcResult() {
    }

    public RpcResult(int timeout) {
        this.timeout = timeout;
    }

    public RpcResult(Object msg) {
        this.msg = msg;
    }

    public Object get() {
        return get(this.timeout);
    }

    public Object get(int timeout) {
        if (finished) {
            return this.msg;
        } else {
            try {
                finishCondition.await(timeout, TimeUnit.MILLISECONDS);
                if (!finished) {
                    throw new RuntimeException("Get result was timeout");
                } else {
                    return this.msg;
                }
            } catch (InterruptedException e) {
                logger.error("Get result was interrupted");
                throw new RuntimeException("Get result was interrupted");
            }
        }
    }
}
