package top.aprilyolies.beehive.transporter.server.serializer.hessian;

import com.alibaba.com.caucho.hessian.io.SerializerFactory;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class HessianSerializerFactory extends SerializerFactory {

    public static final SerializerFactory SERIALIZER_FACTORY = new HessianSerializerFactory();

    private HessianSerializerFactory() {
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
