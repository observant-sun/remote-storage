package kriuchkov.maksim.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import kriuchkov.maksim.common.MessageTypeDecoder;
import kriuchkov.maksim.common.OutboundMessageSplitter;
import kriuchkov.maksim.common.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {

    private static Logger logger = LogManager.getLogger(Server.class);

    public Server() {

    }

    public void launch(int port) throws Throwable {
        logger.info("Starting server on port " + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            // out
                            socketChannel.pipeline().addLast(new OutboundMessageSplitter());

                            // in
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Protocol.MAX_FRAME_BODY_LENGTH, 1, 4));
                            socketChannel.pipeline().addLast(new MessageTypeDecoder());
                            socketChannel.pipeline().addLast(new FinalHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("Server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            logger.info("Shutting down server");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
