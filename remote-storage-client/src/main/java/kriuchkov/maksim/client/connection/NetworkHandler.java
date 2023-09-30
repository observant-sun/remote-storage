package kriuchkov.maksim.client.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import kriuchkov.maksim.common.MessageTypeDecoder;
import kriuchkov.maksim.common.OutboundMessageSplitter;
import kriuchkov.maksim.common.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

class NetworkHandler {
    private Channel channel;
    private IncomingDataReader incomingDataReader;

    private static final Logger logger = LogManager.getLogger(NetworkHandler.class);


    private static NetworkHandler instance = new NetworkHandler();

    private NetworkHandler() {

    }

    public static NetworkHandler getInstance() {
        return instance;
    }

    public void launch(CountDownLatch countDownLatch, String address, int port, IncomingDataReader incomingDataReader) throws Throwable {
        logger.info("Connecting to " + address + ":" + port);

        this.incomingDataReader = incomingDataReader;

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(address, port)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            channel = socketChannel;

                            // out
                            channel.pipeline().addLast(new OutboundMessageSplitter());

                            // in
                            channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Protocol.MAX_FRAME_BODY_LENGTH, 1, 4));
                            channel.pipeline().addLast(new MessageTypeDecoder());
                            channel.pipeline().addLast(incomingDataReader);
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();
            countDownLatch.countDown();
            logger.info("Connection to " + address + ":" + port + " established");
            future.channel().closeFuture().sync();
        } finally {
            logger.info("Disconnecting from " + address + ":" + port);
            group.shutdownGracefully().sync();
        }
    }

    public void stop() {
        logger.info("Asked to stop.");
        // TODO: break connection somehow
    }

    public Channel getChannel() {
        return channel;
    }

    public IncomingDataReader getIncomingDataReader() {
        return incomingDataReader;
    }
}
