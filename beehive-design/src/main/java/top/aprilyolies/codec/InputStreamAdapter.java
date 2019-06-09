package top.aprilyolies.codec;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class InputStreamAdapter extends InputStream {
    ByteBuf buf;

    public InputStreamAdapter(ByteBuf in) {
        this.buf = in;
    }

    @Override
    public int read() throws IOException {
        return buf.readByte();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readable = buf.readableBytes();
        len = Math.min(readable, len);
        buf.readBytes(b, off, len);
        return len;
    }
}
