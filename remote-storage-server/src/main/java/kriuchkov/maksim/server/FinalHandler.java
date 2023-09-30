package kriuchkov.maksim.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import kriuchkov.maksim.common.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FinalHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(FinalHandler.class);

    private ServerFileService fileService;
    private ServerCommandService commandService;

    private final byte[] buffer = new byte[Protocol.MAX_FRAME_BODY_LENGTH];

    public FinalHandler() {
        logger.trace("Creating new FinalHandler");
        fileService = new ServerFileService();
        commandService = new ServerCommandService(fileService);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.catching(cause);
        fileService.close();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.trace("channelRead()");
        if (msg instanceof String) {
            String command = (String) msg;
            commandService.parseAndExecute(command, ctx.channel());
        } else {
            ByteBuf data = (ByteBuf) msg;
            fileService.receiveData(data);
        }
    }

}
