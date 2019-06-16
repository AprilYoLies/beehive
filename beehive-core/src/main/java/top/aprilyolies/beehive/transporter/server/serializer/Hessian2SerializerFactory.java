package top.aprilyolies.beehive.transporter.server.serializer;

import com.alibaba.com.caucho.hessian.io.SerializerFactory;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class Hessian2SerializerFactory extends SerializerFactory {

    public static final SerializerFactory SERIALIZER_FACTORY = new Hessian2SerializerFactory();

    private Hessian2SerializerFactory() {
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
