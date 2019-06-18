package top.aprilyolies.beehive.transporter;

import io.netty.channel.ChannelHandlerContext;
import top.aprilyolies.beehive.common.BeehiveContext;
import top.aprilyolies.beehive.common.RpcInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.common.UrlConstants;
import top.aprilyolies.beehive.common.result.Result;
import top.aprilyolies.beehive.invoker.Invoker;
import top.aprilyolies.beehive.transporter.server.message.MessageType;
import top.aprilyolies.beehive.transporter.server.message.Request;
import top.aprilyolies.beehive.transporter.server.message.Response;
import top.aprilyolies.beehive.utils.ClassUtils;

/**
 * @Author EvaJohnson
 * @Date 2019-06-17
 * @Email g863821569@gmail.com
 */
public class EventHandleThread implements Runnable {
    private final Object msg;

    private final URL url;
    private final ChannelHandlerContext ctx;

    public EventHandleThread(ChannelHandlerContext ctx, URL url, Object msg) {
        this.msg = msg;
        this.url = url;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        try {
            handleMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理接收到的消息，此处的处理怎么看都不够优雅
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void handleMsg() throws IllegalAccessException, InstantiationException {
        // 这里是对于非事件 request 的处理方式
        if (msg instanceof Request) {
            Request request = (Request) msg;
            if (!request.isEvent()) {
                // 根据 request 相关的内容构建 response
                Response response = new Response();
                response.setId(request.getId());
                response.setType(MessageType.RESPONSE);
                response.setStatus(Response.OK);
                Object data = request.getData();
                // 如果 request 携带的数据为 RpcInfo，那么就根据其进行相应的 invoker 调用
                if (data instanceof RpcInfo) {
                    RpcInfo info = (RpcInfo) data;
                    // 尝试从 BeehiveContext 中获取 invoker 实例
                    Invoker invoker = BeehiveContext.safeGet(info.getServiceName(), Invoker.class);
                    // 根据 url 信息获取 invoke target 实例
                    Class<?> clazz = ClassUtils.forName(url.getParameter(UrlConstants.SERVICE_REF));
                    Object target = clazz.newInstance();
                    // 进行真正的 invoke 操作
                    Result result = invoker.invoke(info.createInvokeInfo(target));
                    // 将 invoke 的结果填充到 response 中
                    response.setData(result);
                    // 将响应结果写回
                    ctx.writeAndFlush(response);
                }
            }
        } else if (msg instanceof Response) {

        }
    }
}
