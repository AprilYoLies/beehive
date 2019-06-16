package top.aprilyolies.beehive.transporter.server.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @Author EvaJohnson
 * @Date 2019-06-16
 * @Email g863821569@gmail.com
 */
public interface InputSerializer extends Serializer {
    /**
     * Read boolean.
     *
     * @return boolean.
     * @throws IOException
     */
    boolean readBool() throws IOException;

    /**
     * Read byte.
     *
     * @return byte value.
     * @throws IOException
     */
    byte readByte() throws IOException;

    /**
     * Read short integer.
     *
     * @return short.
     * @throws IOException
     */
    short readShort() throws IOException;

    /**
     * Read integer.
     *
     * @return integer.
     * @throws IOException
     */
    int readInt() throws IOException;

    /**
     * Read long.
     *
     * @return long.
     * @throws IOException
     */
    long readLong() throws IOException;

    /**
     * Read float.
     *
     * @return float.
     * @throws IOException
     */
    float readFloat() throws IOException;

    /**
     * Read double.
     *
     * @return double.
     * @throws IOException
     */
    double readDouble() throws IOException;

    /**
     * Read UTF-8 string.
     *
     * @return string.
     * @throws IOException
     */
    String readUTF() throws IOException;

    /**
     * Read byte array.
     *
     * @return byte array.
     * @throws IOException
     */
    byte[] readBytes() throws IOException;

    /**
     * read object
     *
     * @return object
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if an ClassNotFoundException occurs
     */
    Object readObject() throws IOException, ClassNotFoundException;

    /**
     * read object
     *
     * @param cls object class
     * @return object
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if an ClassNotFoundException occurs
     */
    <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException;

    /**
     * read object
     *
     * @param cls  object class
     * @param type object type
     * @return object
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if an ClassNotFoundException occurs
     */
    <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException;
}
