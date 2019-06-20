package top.aprilyolies.beehive.transporter;

import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.annotation.SPI;
import top.aprilyolies.beehive.transporter.client.Client;
import top.aprilyolies.beehive.transporter.server.Server;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */

/**
 * 这是 beehive 底层通信的顶层接口，beehive 会根据 url 的 transporterSelector 参数来决定使用哪种底层通信的方式，默认为 netty
 */
@SPI("netty")
public interface Transporter {
    /**
     * 服务器的绑定操作
     *
     * @param url
     * @return
     */
    Server bind(URL url);

    /**
     * 客户端的准备
     *
     * @param url
     * @return
     */
    Client connect(URL url);
}
