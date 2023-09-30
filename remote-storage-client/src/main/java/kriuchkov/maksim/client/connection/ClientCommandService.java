package kriuchkov.maksim.client.connection;

import kriuchkov.maksim.common.CommandService;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

class ClientCommandService extends CommandService {

    private static final ClientCommandService instance = new ClientCommandService();

    public static ClientCommandService getInstance() {
        return instance;
    }

    private String expectedResponse = null;

    private Consumer<List<String>> listDataConsumer;

    public void parseAndExecute(String input) throws Exception {
        String[] split = input.split("[\\s\n]", 2);
        String command = split[0];

        assertResponseExpected(command);

        switch (command) {
            case "AUTH-RESP":
                if (split[1].equals("OK")) {
                    MainService.getInstance().setAuthorized(true);
                    MainService.getInstance().getAuthSuccess().run();
                } else {
                    MainService.getInstance().setAuthorized(false);
                    MainService.getInstance().getAuthFailure().accept("Authorization failed. Server response: " + split[1]);
                }
                break;

            case "LIST-RESP":
                String[] fileNames = split[1].split("\n");
                listDataConsumer.accept(Arrays.asList(fileNames));
                break;

            case "FETCH-RESP":
                if (!split[1].split(" ")[0].equals("OK")) {
                    ClientFileService.getInstance().setDataTarget(null);
                    MainService.getInstance().getFetchFailure().accept("Fetch failed. Server response: " + split[1]);
                } else {
                    ClientFileService.getInstance().setExpectedDataLength(Long.parseLong(split[1].split(" ")[2]));
                }
                break;

            case "STORE-RESP":
                if (!split[1].split(" ")[0].equals("OK")) {
                    ClientFileService.getInstance().setDataSource(null);
                    MainService.getInstance().getStoreFailure().accept("Store failed. Server response: " + split[1]);
                } else {
                    logger.debug("About to call doStore()");
                    logger.debug("fileService = " + ClientFileService.getInstance().toString());
                    if (MainService.getInstance() != null)
                        logger.debug("mainService = " + MainService.getInstance().toString());
                    else
                        logger.debug("mainService = null");
                    logger.debug("storeSuccess = " + MainService.getInstance().getStoreSuccess());
                    ClientFileService.getInstance().doStore(MainService.getInstance().getStoreSuccess());
                }
                break;

            case "REMOVE-RESP":
                if (!split[1].equals("OK")) {
                    MainService.getInstance().getDeleteFailure().accept("Deleting remote file failed. Server response: " + split[1]);
                } else {
                    MainService.getInstance().getDeleteSuccess().run();
                }
                break;
        }
    }

    public void expectResponse(String s) {
        logger.debug("expecting server response " + s);
        expectedResponse = s;
    }

    public void assertResponseExpected(String response) {
        if (!response.equals(expectedResponse)) {
            logger.error("Received an unexpected response from server: " + response);
            throw new RuntimeException("Unexpected response from server: " + response);
        }
    }

    public void setListDataConsumer(Consumer<List<String>> listDataConsumer) {
        this.listDataConsumer = listDataConsumer;
    }
}
