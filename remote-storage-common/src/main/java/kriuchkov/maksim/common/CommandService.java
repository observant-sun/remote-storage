package kriuchkov.maksim.common;

import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandService {

    protected static final Logger logger = LogManager.getLogger(CommandService.class);

    public static void sendMsg(String msg, Channel channel) {
        logger.trace("sendMsg() run");
        channel.writeAndFlush(msg);
    }

}
