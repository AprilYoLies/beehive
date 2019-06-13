package top.aprilyolies.beehive.protocol;


import top.aprilyolies.beehive.common.URL;

/**
 * @Author EvaJohnson
 * @Date 2019-06-13
 * @Email g863821569@gmail.com
 */
public abstract class AbstractRegistryProtocol extends AbstractProtocol {
    @Override
    public void publish(URL url) {
        if (url == null)
            throw new IllegalArgumentException("Can't publish a service for null url");
        try {
            doPublish(url);
        } catch (Exception e) {
            logger.error("publish service failed", e.getCause());
        }
    }

    protected abstract void doPublish(URL url) throws Exception;
}
