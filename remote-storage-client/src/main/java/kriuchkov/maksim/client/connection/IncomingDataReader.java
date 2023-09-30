package kriuchkov.maksim.client.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class IncomingDataReader extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(IncomingDataReader.class);

    public IncomingDataReader() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof String) {
            logger.debug("Message received");
            String command = (String) msg;
            ClientCommandService.getInstance().parseAndExecute(command);
        } else {
            logger.debug("Data received");
            ByteBuf data = (ByteBuf) msg;
            ClientFileService.getInstance().receiveData(data);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.catching(cause);
        ctx.close();
    }
}
