package kriuchkov.maksim.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageTypeDecoder extends ByteToMessageDecoder {

    private static Logger logger = LogManager.getLogger(MessageTypeDecoder.class);

    public MessageTypeDecoder() {
//        setSingleDecode(true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() > 0) {
            if (in.getByte(0) == Protocol.COMMAND_SIGNAL_BYTE) {
                logger.debug("New frame, decoded as command frame, readableBytes = " + in.readableBytes());
                discardHeader(in);
                out.add(in.toString(StandardCharsets.UTF_8));
                in.skipBytes(in.readableBytes());
            } else if (in.getByte(0) == Protocol.DATA_SIGNAL_BYTE) {
                logger.debug("New frame, decoded as command frame, readableBytes = " + in.readableBytes());
                discardHeader(in);
                out.add(in);
            } else {
                logger.warn("New frame, decoded as corrupted, readableBytes = " + in.readableBytes());
                throw new CorruptedFrameException("Expected a signal byte in front of frame, but it was not there.");
            }
        }
    }

    private void discardHeader(ByteBuf in) {
        in.skipBytes(1 + 4);
        in.discardReadBytes();
    }
}
