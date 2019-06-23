package top.aprilyolies.beehive.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @Author EvaJohnson
 * @Date 2019-06-12
 * @Email g863821569@gmail.com
 */
public class RegistryConfigBean extends AbstractConfig {
    // 服务的注册地址
    private String[] address;
    //
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getAddress() {
        return address;
    }

    public void setAddress(String[] address) {
        this.address = address;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // empty
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // empty
    }
}
