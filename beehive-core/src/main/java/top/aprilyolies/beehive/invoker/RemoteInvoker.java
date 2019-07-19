package top.aprilyolies.beehive.invoker;

import io.netty.channel.Channel;
import top.aprilyolies.beehive.common.*;
import top.aprilyolies.beehive.common.result.RpcResult;
import top.aprilyolies.beehive.transporter.client.Client;
import top.aprilyolies.beehive.transporter.server.message.MessageType;
import top.aprilyolies.beehive.transporter.server.message.Request;
import top.aprilyolies.beehive.utils.StringUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author EvaJohnson
 * @Date 2019-06-19
 * @Email g863821569@gmail.com
 */
public class RemoteInvoker extends AbstractInvoker {
    // 用于连接的地址
    private final String host;
    // 用于连接的端口
    private final int port;
    // 连接的客户端
    private final Client client;
    // 获取结果超时，请求重发的次数
    private final int RETRY_TIMES = 3;
    // 缓存原 provider 的信息
    private final String provider;
    // URL 信息
    private final URL url;
    // 用于存储当前线程建立的 channel 信息
    private final ThreadLocal<Map<String, Channel>> addressChannel = new AddressChannelThreadLocal();
    // 客户端 channel 缓存的 key 信息
    private final String channelKey;

    public String getProvider() {
        return provider;
    }

    public RemoteInvoker(String host, int port, Client client, String provider, URL url) {
        this.host = host;
        this.port = port;
        this.client = client;
        this.channelKey = host + ":" + port;
        this.provider = provider;
        this.url = url;
    }

    @Override
    protected Object doInvoke(InvokeInfo info) {
        try {
            if (port < 0) {
                throw new IllegalArgumentException("The port " + port + " is less than 0");
            }
            if (client == null) {
                throw new IllegalStateException("None of client could be use, the client was null");
            }
            // 获取当前线程的 channelMap
            Map<String, Channel> channelMap = addressChannel.get();
            Channel ch = channelMap.get(channelKey);
            if (ch == null || !ch.isOpen() || !ch.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Thread of " + Thread.currentThread().getName() + " don't hold the channel to service " +
                            "provider: " + host + ":" + port);
                }
                // 连接服务器
                ch = connectServer();
                if (ch != null && ch.isActive() && ch.isOpen()) {
                    // 进行缓存
                    channelMap.put(channelKey, ch);
                } else {
                    channelMap.remove(channelKey);
                    return null;
                }
            }
            // 构建 request 消息
            Request request = buildRequest(info);
            // 发送消息
            System.out.println(ch);
            ch.writeAndFlush(request);
            // 获取异步的响应结果
            Object result = getResponse(request);
            String retryTimes = this.url.getParameter(UrlConstants.RETRY_TIMES);
            int times = RETRY_TIMES;
            if (!StringUtils.isEmpty(retryTimes)) {
                try {
                    times = Integer.parseInt(retryTimes);
                } catch (NumberFormatException e) {
                    logger.warn("The retry times parameter " + retryTimes + " is wrong, use the default retry times " + RETRY_TIMES);
                    times = RETRY_TIMES;
                }
            }
            int retryCount = 1;
            while (result == null && retryCount <= times) {
                logger.info("Got result of request " + request + " timeout, attempt to retry 3 times, this is " + retryCount++ + " time");
                // 发送消息
                ch.writeAndFlush(request);
                result = getResponse(request);
            }
            if (result == null && logger.isDebugEnabled()) {
                logger.info("Send request " + request + " 3 times, but the result was still null");
            }
//        client.disconnect();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 BeehiveContext 中获取异步的响应结果
     *
     * @param request
     * @return
     */
    private Object getResponse(Request request) {
        // 拿到请求的 id，用于异步获取响应内容
        String sid = String.valueOf(request.getId());
        // 存根请求结果
        BeehiveContext.unsafePut(sid, new RpcResult());
        // 异步获取相应结果
        Object res = null;
        try {
            String timeout = this.url.getParameter(UrlConstants.READ_TIMEOUT);
            if (StringUtils.isEmpty(timeout)) {
                res = BeehiveContext.unsafeGet(sid, RpcResult.class).get();
            } else {
                try {
                    int time = Integer.parseInt(timeout);
                    res = BeehiveContext.unsafeGet(sid, RpcResult.class).get(time);
                } catch (NumberFormatException e) {
                    logger.warn("The timeout parameter " + timeout + " is wrong, use the default timeout 2000ms");
                    res = BeehiveContext.unsafeGet(sid, RpcResult.class).get();
                }
            }
        } catch (Throwable t) {
            // empty
        }
        // 移除缓存
        BeehiveContext.unsafeRemove(sid);
        return res;
    }

    // 打开到服务端的连接，这里默认公用同一个 client
    private Channel connectServer() {
        InetSocketAddress address = new InetSocketAddress(host, port);
        return client.connect(address);
    }

    /**
     * 构建请求消息
     *
     * @param info
     * @return 请求消息
     */
    private Request buildRequest(InvokeInfo info) {
        RpcInfo rpcInfo = info.buildRpcInfo();
        Request request = new Request();
        request.setType(MessageType.REQUEST);
        request.setData(rpcInfo);
        return request;
    }

    /**
     * 线程本地变量，用于承载 address 到 channel 的映射
     */
    private class AddressChannelThreadLocal extends ThreadLocal<Map<String, Channel>> {
        @Override
        protected Map<String, Channel> initialValue() {
            return new HashMap<>();
        }
    }
}
