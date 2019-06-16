package top.aprilyolies.beehive.transporter.server.serializer;

import java.io.IOException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public interface OutputSerializer extends Serializer {

    /**
     * Write boolean.
     *
     * @param v value.
     * @throws IOException
     */
    void writeBool(boolean v) throws IOException;

    /**
     * Write byte.
     *
     * @param v value.
     * @throws IOException
     */
    void writeByte(byte v) throws IOException;

    /**
     * Write short.
     *
     * @param v value.
     * @throws IOException
     */
    void writeShort(short v) throws IOException;

    /**
     * Write integer.
     *
     * @param v value.
     * @throws IOException
     */
    void writeInt(int v) throws IOException;

    /**
     * Write long.
     *
     * @param v value.
     * @throws IOException
     */
    void writeLong(long v) throws IOException;

    /**
     * Write float.
     *
     * @param v value.
     * @throws IOException
     */
    void writeFloat(float v) throws IOException;

    /**
     * Write double.
     *
     * @param v value.
     * @throws IOException
     */
    void writeDouble(double v) throws IOException;

    /**
     * Write string.
     *
     * @param v value.
     * @throws IOException
     */
    void writeUTF(String v) throws IOException;

    /**
     * Write byte array.
     *
     * @param v value.
     * @throws IOException
     */
    void writeBytes(byte[] v) throws IOException;

    /**
     * Write byte array.
     *
     * @param v   value.
     * @param off the start offset in the data.
     * @param len the number of bytes that are written.
     * @throws IOException
     */
    void writeBytes(byte[] v, int off, int len) throws IOException;

    /**
     * Flush buffer.
     *
     * @throws IOException
     */
    void flushBuffer() throws IOException;

    /**
     * write object.
     *
     * @param obj object.
     */
    void writeObject(Object obj) throws IOException;
}
