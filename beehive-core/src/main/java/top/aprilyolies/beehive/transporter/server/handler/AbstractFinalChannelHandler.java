package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.transporter.BeehiveThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author EvaJohnson
 * @Date 2019-06-21
 * @Email g863821569@gmail.com
 */
public class AbstractFinalChannelHandler extends ChannelDuplexHandler {
    protected Logger logger = Logger.getLogger(getClass());
    // 默认的 ServerFinalChannelHandler 的线程池线程名
    static final String DEFAULT_THREAD_NAME = "final-channel-handler-thread";
    // 共享的 executor
    static final ExecutorService SHARED_EXECUTOR = Executors.newCachedThreadPool(new BeehiveThreadFactory(DEFAULT_THREAD_NAME, true));
    private final URL url;

    public URL getUrl() {
        return url;
    }

    AbstractFinalChannelHandler(URL url) {
        this.url = url;
    }
}
