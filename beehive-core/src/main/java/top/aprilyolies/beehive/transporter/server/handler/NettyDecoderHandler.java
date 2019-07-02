package top.aprilyolies.beehive.transporter.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;
import top.aprilyolies.beehive.common.RpcInfo;
import top.aprilyolies.beehive.common.URL;
import top.aprilyolies.beehive.extension.ExtensionLoader;
import top.aprilyolies.beehive.transporter.server.message.MessageType;
import top.aprilyolies.beehive.transporter.server.message.Request;
import top.aprilyolies.beehive.transporter.server.message.Response;
import top.aprilyolies.beehive.transporter.server.serializer.InputSerializer;
import top.aprilyolies.beehive.transporter.server.serializer.factory.SerializerFactory;
import top.aprilyolies.beehive.utils.ByteUtils;
import top.aprilyolies.beehive.utils.ReflectUtils;

import java.util.List;

import static top.aprilyolies.beehive.transporter.server.handler.NettyEncoderHandler.REQUEST_FLAG;

/**
 * @Author EvaJohnson
 * @Date 2019-06-15
 * @Email g863821569@gmail.com
 */
public class NettyDecoderHandler extends ByteToMessageDecoder {
    private static final Logger logger = Logger.getLogger(NettyDecoderHandler.class);
    private SerializerFactory extensionSelector = ExtensionLoader.getExtensionLoader(SerializerFactory.class).getExtensionSelectorInstance();
    // 魔幻头，十进制为 7440
    private final short MAGIC = 0x4A28;
    // 空解析结果
    private final Object EMPTY_RESULT = new Object();
    // 序列化器
    private InputSerializer serializer;
    // 请求头长度
    private int HEADER_LENGTH = 16;
    // 魔幻数字高字节
    private byte MAGIC_LOW = (byte) MAGIC;
    // 魔幻数字低字节
    private byte MAGIC_HIGH = (byte) (MAGIC >>> 8);
    // 序列化器掩码
    protected static final int SERIALIZER_MASK = 0x1f;
    // 事件标志V
    protected static final byte EVENT_FLAG = (byte) 0x20;   // event 消息的标志      0010 0000

    private final URL url;

    public NettyDecoderHandler(URL url) {
        this.url = url;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        serializer = extensionSelector.deserializer(url, in);
        while (in.isReadable()) {
            int readerIndex = in.readerIndex();
            Object result = prepareDecode(ctx, in, out);
            if (result == EMPTY_RESULT) {
                in.readerIndex(readerIndex);
                break;
            } else {
                out.add(result);
            }
        }
    }

    private Object prepareDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int readable = in.readableBytes();
        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
        in.readBytes(header);
        // 如果魔幻头不一致，那么就需要重新定位到新的魔幻头
        if ((readable > 0 && header[0] != MAGIC_LOW) || (readable > 1 && header[1] != MAGIC_HIGH)) {
            int length = header.length;
            if (header.length < readable) { // 这里说明够头部的长度
                // 这里就说明，除了 header 的字节部分，还有真正的数据部分
                header = ByteUtils.copyOf(header, readable);
                // 这样就是将全部数据读到了 header 中，前 16 个为 header 部分，后边的为剩余数据
                in.readBytes(header, length, readable - length);    // 全部的数据都到了 header 中
            }
            for (int i = 1; i < header.length - 1; i++) {
                if (header[i] == MAGIC_LOW && header[i + 1] == MAGIC_HIGH) {    // 这里是重新确定魔幻头的位置
                    // 这里就说明又检测到了一个数据包，那么就将第一个数据包的内容放到 header 中
                    in.readerIndex(in.readerIndex() - header.length + i);   // 将 readerIndex 重新定位到新的魔幻头位置
                    readable = in.readableBytes();
                    header = new byte[Math.min(readable, HEADER_LENGTH)];
                    in.readBytes(header);
                    break;
                }
            }
            // 一次只解析一个数据包
        }
        // check length.
        if (readable < HEADER_LENGTH) { // 接收到的数据还不足以解析出一个完整的数据包
            return EMPTY_RESULT;
        }

        // get data length. 根据 header 获取 len 信息
        int len = ByteUtils.readInt(header, 12);
        // 总长度为 header + 数据长度（header 后四位进行记录）
        int tt = len + HEADER_LENGTH;
        if (readable < tt) {    // 也就是说目前接受到的数据还不够 header + 包长度得到的总长度
            return EMPTY_RESULT;
        }

        // limit input stream.通过 ChannelBufferInputStream 对 buffer 进行封装，记录了内容读取的上下界
        // ChannelBufferInputStream -> NettyBackedCahnnelBuffer -> Netty 原生 buf
        return doDecode(header); // 解码除开 header 以外的信息
    }

    private Object doDecode(byte[] header) {
        byte flag = header[2], proto = (byte) (flag & SERIALIZER_MASK);
        // get request id.
        long id = ByteUtils.readLong(header, 4);
        // 如果标志位是不是 request，那就说明是收到的 response
        if ((flag & REQUEST_FLAG) == 0) {
            // decode response.
            Response res = new Response(id);
            // get status.
            byte status = header[3];
            res.setStatus(status);
            // 判断当前相应是否为事件响应
            try {
                try {
                    if ((flag & EVENT_FLAG) == 0) {
                        res.setType(MessageType.RESPONSE);
                        Object msg = serializer.readObject();
                        res.setData(msg);
                    } else {
                        res.setType(MessageType.HEARTBEAT_RESPONSE);
                        Object msg = serializer.readObject();
                        res.setData(msg);
                    }
                } catch (Exception e) {
                    logger.error("Decode message error, please check provider and consumer use the same serializer");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return res;
        } else {
            // decode request.
            Request req = new Request(id);
            try {
                try {
                    // 判断是否是事件类型的 request
                    if ((flag & EVENT_FLAG) == 0) {
                        // 设置 req 为非事件类型的 request
                        req.setType(MessageType.REQUEST);
                        String serviceName = serializer.readUTF();
                        // 逆编码过程，逐个提取出对应的属性
                        String methodName = serializer.readUTF();
                        String ptsDesc = serializer.readUTF();
                        // 将参数 desc 串转换为对应的 class
                        Class<?>[] pts = ReflectUtils.desc2classArray(ptsDesc);
                        Object[] pvs = new Object[pts.length];
                        // 根据参数类型提取出每个参数
                        for (int i = 0; i < pvs.length; i++) {
                            pvs[i] = serializer.readObject(pts[i]);
                        }
                        // 重构 RpcInfo 信息
                        RpcInfo info = new RpcInfo(methodName, pts, pvs, serviceName);
                        req.setData(info);
                    } else {
                        Object msg = serializer.readObject();
                        req.setType(MessageType.HEARTBEAT_REQUEST);
                        req.setData(msg);
                    }
                } catch (Exception e) {
                    logger.error("Decode message error, please check provider and consumer use the same serializer");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return EMPTY_RESULT;
            }
            return req;
        }
    }
}
