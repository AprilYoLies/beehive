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
    private ReentrantLock lock = new ReentrantLock();
    // 结果完成的条件
    private Condition finishCondition = lock.newCondition();
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

    /**
     * 带延时的获取相应的结果
     *
     * @param timeout 延时时长
     * @return 相应的结果
     */
    public Object get(int timeout) {
        if (finished) {
            return this.msg;
        } else {
            try {
                lock.lock();
                finishCondition.await(timeout, TimeUnit.MILLISECONDS);
                if (!finished) {
                    throw new RuntimeException("Get result was timeout");
                } else {
                    return this.msg;
                }
            } catch (InterruptedException e) {
                logger.error("Get result was interrupted");
                throw new RuntimeException("Get result was interrupted");
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 填充响应的结果，同时唤醒等待响应结果的线程
     *
     * @param data
     */
    public void fillData(Object data) {
        if (!finished) {
            try {
                lock.lock();
                this.msg = data;
                finished = true;
                finishCondition.notify();
            } finally {
                lock.unlock();
            }
        }
    }
}
