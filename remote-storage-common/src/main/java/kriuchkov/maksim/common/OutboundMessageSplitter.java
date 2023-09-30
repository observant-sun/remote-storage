package kriuchkov.maksim.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class OutboundMessageSplitter extends MessageToMessageEncoder<Object> {

    private static Logger logger = LogManager.getLogger(OutboundMessageSplitter.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, List<Object> out) throws Exception {
        if (o instanceof String) {
            String msg = (String) o;
            logger.debug("Encoding and passing a string: " + msg);
            ByteBuf byteBuf;

            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);

            if (msgBytes.length > Protocol.MAX_FRAME_BODY_LENGTH) {
                throw new RuntimeException("Command too long to send!");
            }

            byteBuf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + msgBytes.length);
            byteBuf.writeByte(Protocol.COMMAND_SIGNAL_BYTE);
            byteBuf.writeInt(msgBytes.length);
            byteBuf.writeBytes(msgBytes);
            ctx.writeAndFlush(byteBuf);
        } else {
            ByteBuf in = (ByteBuf) o;
            logger.debug("Encoding and passing a ByteBuf, readableBytes = " + in.readableBytes());
            sendSplitDataFrames(in, out);
        }
    }

    private void sendSplitDataFrames(ByteBuf in, List<Object> out) {
        try {
            while (in.readableBytes() > 0) {
                int frameBodyLength = Math.min(in.readableBytes(), Protocol.MAX_FRAME_BODY_LENGTH);
                ByteBuf outFrame = ByteBufAllocator.DEFAULT.buffer(1 + 4 + frameBodyLength);
                outFrame.writeByte(Protocol.DATA_SIGNAL_BYTE);
                outFrame.writeInt(frameBodyLength);
                outFrame.writeBytes(in, frameBodyLength);
                out.add(outFrame);
            }
        } finally {
//            in.release();
        }
    }

}
