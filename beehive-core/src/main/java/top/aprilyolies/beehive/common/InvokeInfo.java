package top.aprilyolies.beehive.common;

/**
 * @Author EvaJohnson
 * @Date 2019-06-10
 * @Email g863821569@gmail.com
 */
public class InvokeInfo {
    // 待调用的方法的名字
    private String methodName;
    // 方法的餐宿类型
    private Class<?>[] pts;
    // 方法的参数值
    private Object[] pvs;
    // 方法调用的实例对象
    private Object target;
    // 服务名字
    private String serviceName;

    public InvokeInfo(String methodName, Class<?>[] pts, Object[] pvs, Object target, String serviceName) {
        this.methodName = methodName;
        this.pts = pts;
        this.pvs = pvs;
        this.target = target;
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getPts() {
        return pts;
    }

    public void setPts(Class<?>[] pts) {
        this.pts = pts;
    }

    public Object[] getPvs() {
        return pvs;
    }

    public void setPvs(Object[] pvs) {
        this.pvs = pvs;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public RpcInfo buildRpcInfo() {
        return new RpcInfo(methodName, pts, pvs, serviceName);
    }
}
