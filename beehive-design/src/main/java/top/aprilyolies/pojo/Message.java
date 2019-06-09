package top.aprilyolies.pojo;

import java.io.Serializable;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class Message implements Serializable {
    private String msg;

    public Message(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
