package kriuchkov.maksim.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedNioFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class FileService implements Closeable {

    protected static final Logger logger = LogManager.getLogger(FileService.class);

    protected static final int FILE_CHUNK_SIZE = Protocol.MAX_FRAME_BODY_LENGTH / 2;

    public static void sendFile(File file, Channel channel, Runnable callback) throws Exception {
        logger.trace("sendFile(): file = " + file.toPath().toAbsolutePath().toString());
        ChunkedNioFile chunkedNioFile = null;
        try {
            chunkedNioFile = new ChunkedNioFile(file, FILE_CHUNK_SIZE);

            while (!chunkedNioFile.isEndOfInput()) {
                ByteBuf next = chunkedNioFile.readChunk(ByteBufAllocator.DEFAULT);
                channel.writeAndFlush(next);
            }

            logger.info("File " + file.toPath().toAbsolutePath() + " was fully sent");

            if (callback != null) {
                logger.trace("sendFile() callback run");
                callback.run();
            }
        } finally {
            if (chunkedNioFile != null)
                chunkedNioFile.close();
        }

    }

    protected File dataTarget;
    protected long length;

    protected File dataSource;

    protected FileOutputStream fos;

    protected final byte[] buffer = new byte[Protocol.MAX_FRAME_BODY_LENGTH];

    public void setDataTarget(File dataTarget) throws FileNotFoundException {
        if (fos != null) {
            try {
                closeFOS();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataTarget = dataTarget;
        if (dataTarget != null) {
            logger.debug("data target set to " + dataTarget.toPath().toAbsolutePath().toString());
            this.fos = new FileOutputStream(dataTarget);
            logger.debug("FileOutputStream for file " + dataTarget.toPath().toAbsolutePath() + " was created");
        } else {
            logger.debug("data target set to null");
        }
    }

    public void setDataSource(File dataSource) throws FileNotFoundException {
        if (dataSource != null)
            logger.debug("data source set to " + dataSource.toPath().toAbsolutePath().toString());
        else
            logger.debug("data source set to null");
        this.dataSource = dataSource;
        if (dataSource != null && !dataSource.exists()) {
            logger.warn("data source file " + dataSource.toPath().toAbsolutePath().toString() + " doesn't seem to exist");
        }
    }

    public void receiveData(ByteBuf data) throws IOException {
        logger.trace("receiveData() run");
        if (dataTarget == null)
            throw new RuntimeException("Unexpected data block.");

        int l = data.readableBytes();
        if (l > length)
            throw new RuntimeException("More data in block than expected.");
        data.readBytes(buffer, 0, l);
        fos.write(buffer, 0, l);
        fos.flush();
        length -= l;
        if (length == 0) {
            logger.info("File " + dataTarget.toPath().toAbsolutePath() + " fully received");
            setDataTarget(null);
        }
    }

    private void closeFOS() throws IOException {
        if (fos != null) {
            fos.close();
            if (dataTarget != null)
                logger.debug("FileOutputStream for file " + dataTarget.toPath().toAbsolutePath() + " was closed");
            else
                logger.debug("FileOutputStream was closed while dataTarget was null");
        }
    }

    @Override
    public void close() throws IOException {
        logger.trace("close() invoked");
        if (fos != null)
            fos.close();
    }

    public void setExpectedDataLength(long length) {
        logger.debug("expected length set to " + length);
        this.length = length;
    }


}
