package kriuchkov.maksim.client.connection;

import io.netty.buffer.ByteBuf;
import kriuchkov.maksim.common.FileService;

import java.io.IOException;

class ClientFileService extends FileService {

    private static final ClientFileService instance = new ClientFileService();

    public static ClientFileService getInstance() {
        return instance;
    }


    public void doStore(Runnable callback) throws Exception {
        logger.debug("doStore!");
        sendFile(dataSource, NetworkHandler.getInstance().getChannel(), callback);
    }

    @Override
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
            MainService.getInstance().getFetchSuccess().run();
        }
    }
}
