package top.aprilyolies.beehive.transporter.server.serializer.hessian;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;

import java.io.IOException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class HessianSerializer implements OutputSerializer {
    private final Hessian2Output h2o;

    public HessianSerializer(Hessian2Output h2o) {
        this.h2o = h2o;
        h2o.setSerializerFactory(HessianSerializerFactory.SERIALIZER_FACTORY);
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        h2o.writeBoolean(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        h2o.writeBytes(new byte[]{v});
    }

    @Override
    public void writeShort(short v) throws IOException {
        h2o.writeInt(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        h2o.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        h2o.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        h2o.writeDouble(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        h2o.writeDouble(v);
    }

    @Override
    public void writeBytes(byte[] b) throws IOException {
        h2o.writeBytes(b);
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) throws IOException {
        h2o.writeBytes(b, off, len);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        h2o.writeString(v);
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        h2o.writeObject(obj);
    }

    @Override
    public void flushBuffer() throws IOException {
        h2o.flushBuffer();
    }
}
