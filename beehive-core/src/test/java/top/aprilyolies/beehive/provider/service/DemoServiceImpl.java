package top.aprilyolies.beehive.provider.service;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public class DemoServiceImpl implements DemoService {
    @Override
    public String say(String msg) {
        return msg + " world";
    }
}
