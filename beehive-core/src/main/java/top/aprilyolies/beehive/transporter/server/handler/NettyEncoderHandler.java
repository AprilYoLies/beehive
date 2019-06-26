package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.RpcInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.transporter.server.message.Request;
import top.aprilyolies.beehive.transporter.server.message.Response;
import top.aprilyolies.beehive.transporter.server.serializer.OutputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.factory.SerializerFactory;
import top.aprilyolies.beehive.utils.ByteUtils;
import top.aprilyolies.beehive.utils.ReflectUtils;

import java.io.IOException;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class NettyEncoderHandler extends MessageToByteEncoder {
    private static final Logger logger = Logger.getLogger(NettyEncoderHandler.class);
    private SerializerFactory extensionSelector = ExtensionLoader.getExtensionLoader(SerializerFactory.class).getExtensionSelectorInstance();
    // 魔幻头，十进制为 7440
    private final short MAGIC = 0x4A28;
    // 服务 url
    private final URL url;
    // 请求标志
    protected static final byte REQUEST_FLAG = (byte) 0x80; // request 消息的标志    1000 0000
    // 事件标志V
    protected static final byte EVENT_FLAG = (byte) 0x20;   // event 消息的标志      0010 0000
    // 序列化器
    private OutputSerializer serializer;
    // 请求头长度
    private int HEADER_LENGTH = 16;

    public NettyEncoderHandler(URL url) {
        this.url = url;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        serializer = extensionSelector.serializer(url, out);
        if (msg instanceof Request) {
            requestEncode(ctx, msg, out);
        } else if (msg instanceof Response) {
            responseEncode(ctx, msg, out);
        }
    }

    private void requestEncode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws IOException {
        Request request = (Request) msg;
        if (logger.isDebugEnabled()) {
            logger.debug("Encode request message of " + request);
        }
        // 构建请求头
        byte[] header = new byte[HEADER_LENGTH];
        // 填充魔幻数字
        ByteUtils.fillShort(MAGIC, header, 0);
        // 填充请求头的标志字节
        header[2] = (byte) (REQUEST_FLAG | extensionSelector.getSerializerId(url));
        // 填充请求 id
        ByteUtils.fillLong(request.getId(), header, 4);
        // 这是 header 的起始位置
        int headerIndex = out.writerIndex();
        // 将 writer 指针定位到 body 写入的起始位置
        int bodyIndex = headerIndex + HEADER_LENGTH;
        out.writerIndex(bodyIndex);
        // 对于 rpc 请求的编码和事件消息的编码是不一样的
        if (request.isEvent()) {
            header[2] = (byte) (header[2] | EVENT_FLAG);
            encodeEventRequest(request.getData());
        } else {
            encodeRpcRequest(request.getData());
        }
        serializer.flushBuffer();
        int len = out.writerIndex() - bodyIndex;
        // 填充 header 的长度信息
        ByteUtils.fillInt(len, header, 12);
        // 将位置定位到 header 起始位置
        out.writerIndex(headerIndex);
        // 写入头信息
        out.writeBytes(header);
        // 将指针定位到全部数据写入后应该在的位置
        out.writerIndex(bodyIndex + len);
    }

    private void encodeEventRequest(Object msg) throws IOException {
        serializer.writeObject(msg);
    }

    /**
     * 将 rpc 相关的信息写入，不采用直接写入 object 的方式是为了最大的降低数据传输的量
     *
     * @param msg
     * @throws IOException
     */
    private void encodeRpcRequest(Object msg) throws IOException {
        RpcInfo info = (RpcInfo) msg;
        // 请求的服务的名字
        serializer.writeUTF(info.getServiceName());
        // 请求的方法名
        serializer.writeUTF(info.getMethodName());
        // 请求的方法参数类型（用签名的方式表示）
        serializer.writeUTF(ReflectUtils.getDesc(info.getPts()));
        // 方法调用的参数值
        Object[] pvs = info.getPvs();
        if (pvs != null) {
            for (Object pv : pvs) {
                serializer.writeObject(pv);
            }
        }
    }


    private void responseEncode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws IOException {
        Response response = (Response) msg;
        // 响应头
        byte[] header = new byte[HEADER_LENGTH];
        // 响应魔幻头
        ByteUtils.fillShort(MAGIC, header, 0);
        // 序列化器信息
        header[2] = extensionSelector.getSerializerId(url);
        // 响应状态
        byte status = response.getStatus();
        header[3] = status;
        // 响应 id 号
        ByteUtils.fillLong(response.getId(), header, 4);
        int headerIndex = out.writerIndex();
        int bodyIndex = headerIndex + HEADER_LENGTH;
        out.writerIndex(bodyIndex);
        // 对于事件消息和 rpc 响应消息，采用不同的方式编码
        if (response.isEvent()) {
            header[2] = (byte) (header[2] | EVENT_FLAG);
            encodeEventResponse(msg);
        } else {
            encodeRpcResponse(msg);
        }
        serializer.flushBuffer();
        int len = out.writerIndex() - bodyIndex;
        ByteUtils.fillInt(len, header, 12);
        // 填充头信息
        out.writerIndex(headerIndex);
        out.writeBytes(header);
        // 重新定位写索引
        out.writerIndex(bodyIndex + len);
    }

    private void encodeRpcResponse(Object msg) throws IOException {
        Response response = (Response) msg;
        serializer.writeObject(response.getData());
    }

    private void encodeEventResponse(Object msg) throws IOException {
        Response response = (Response) msg;
        serializer.writeObject(response.getData());
    }
}
