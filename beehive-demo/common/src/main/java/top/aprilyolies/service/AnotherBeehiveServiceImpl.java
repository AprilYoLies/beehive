package top.aprilyolies.service;

import java.net.InetAddress;

/**
 * @Author EvaJohnson
 * @Date 2019-07-03
 * @Email g863821569@gmail.com
 */
public class AnotherBeehiveServiceImpl implements BeehiveService {
    @Override
    public String say(String msg) throws Exception {
        return "Jim say " + msg + " from " + InetAddress.getLocalHost().getHostAddress() + ", [ server id is No.2]";
    }

    @Override
    public String see(String msg) throws Exception {
        return "Jim see " + msg + " from " + InetAddress.getLocalHost().getHostAddress() + ", [ server id is No.2]";
    }
}
