package top.aprilyolies.service;

import java.net.InetAddress;

/**
 * @Author EvaJohnson
 * @Date 2019-06-23
 * @Email g863821569@gmail.com
 */
public class BeehiveServiceImpl implements BeehiveService {
    @Override
    public String say(String msg) throws Exception {
        return "Jim say " + msg + " from " + InetAddress.getLocalHost().getHostAddress() + ", [ server id is No.1]";
    }

    @Override
    public String see(String msg) throws Exception {
        return "Jim see " + msg + " from " + InetAddress.getLocalHost().getHostAddress() + ", [ server id is No.1]";
    }
}
