package top.aprilyolies.beehive.transporter.server.serializer;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @Author EvaJohnson
 * @Date 2019-06-09
 * @Email g863821569@gmail.com
 */
public class OutputStreamAdapter extends OutputStream {
    ByteBuf buf;

    public OutputStreamAdapter(ByteBuf out) {
        this.buf = out;
    }

    @Override
    public void write(int b) throws IOException {
        buf.writeByte(b);
    }
}
