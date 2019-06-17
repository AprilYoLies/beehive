package top.aprilyolies.beehive.transporter.server.message;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class Response {
    private long id;

    private MessageType type;

    private Object data;

    private byte status;

    /**
     * ok.
     */
    public static final byte OK = 20;


    public Response() {
    }

    public Response(long id) {
        this.id = id;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public boolean isEvent() {
        return type == MessageType.HEARTBEAT_RESPONSE || type == MessageType.HEARTBEAT_REQUEST;
    }
}
