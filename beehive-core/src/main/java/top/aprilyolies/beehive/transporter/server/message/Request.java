package top.aprilyolies.beehive.transporter.server.message;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class Request {
    private final long id;
    // 全局的 id 生成器
    private static final AtomicLong requestId = new AtomicLong(0);
    // 消息类型
    private MessageType type;
    // 消息附带的内容
    private Object msg;

    public Request() {
        this.id = getRequestId();
    }

    private long getRequestId() {
        return requestId.getAndIncrement();
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public long getId() {
        return id;
    }

    public boolean isEvent() {
        return type == MessageType.HEARTBEAT_RESPONSE || type == MessageType.HEARTBEAT_REQUEST;
    }
}
