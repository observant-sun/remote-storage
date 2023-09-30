package kriuchkov.maksim.server;

public class ServerMain {

    static final int SERVER_PORT = 8189;

    public static void main(String[] args) throws Throwable {
        DatabaseHandler.connect();
        new Server().launch(SERVER_PORT);
    }

}
