package top.aprilyolies.beehive.common.result;

/**
 * @Author EvaJohnson
 * @Date 2019-06-14
 * @Email g863821569@gmail.com
 */
public class RpcResult implements Result {
    // rpc 的
    private Object msg;

    public RpcResult() {
    }

    public RpcResult(Object msg) {
        this.msg = msg;
    }
}
