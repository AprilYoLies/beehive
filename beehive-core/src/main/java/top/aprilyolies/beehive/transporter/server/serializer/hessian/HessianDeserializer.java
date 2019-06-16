package top.aprilyolies.beehive.transporter.server.serializer.hessian;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public class HessianDeserializer implements InputSerializer {
    private final Hessian2Input h2i;

    public HessianDeserializer(Hessian2Input h2i) {
        this.h2i = h2i;
        h2i.setSerializerFactory(HessianSerializerFactory.SERIALIZER_FACTORY);
    }

    @Override
    public boolean readBool() throws IOException {
        return h2i.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) h2i.readInt();
    }

    @Override
    public short readShort() throws IOException {
        return (short) h2i.readInt();
    }

    @Override
    public int readInt() throws IOException {
        return h2i.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return h2i.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return (float) h2i.readDouble();
    }

    @Override
    public double readDouble() throws IOException {
        return h2i.readDouble();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return h2i.readBytes();
    }

    @Override
    public String readUTF() throws IOException {
        return h2i.readString();
    }

    @Override
    public Object readObject() throws IOException {
        return h2i.readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> cls) throws IOException {
        return (T) h2i.readObject(cls);
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException {
        return readObject(cls);
    }
}
