package top.aprilyolies.service;

import java.net.InetAddress;

/**
 * @Author EvaJohnson
 * @Date 2019-07-02
 * @Email g863821569@gmail.com
 */
public class UserServiceImpl implements UserService {
    @Override
    public String findUserById(int id) throws Exception {
        return "Server " + InetAddress.getLocalHost().getHostAddress() + " has found user whose id is " + id;
    }
}
