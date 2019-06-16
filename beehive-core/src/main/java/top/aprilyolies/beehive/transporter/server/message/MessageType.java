package top.aprilyolies.beehive.transporter.server.message;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */

/**
 * 消息类型
 */
public enum MessageType {
    HEARTBEAT_REQUEST(2), HEARTBEAT_RESPONSE(3), REQUEST(0), RESPONSE(1);

    // 消息类型的唯一 ID
    int id;

    MessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
