package kriuchkov.maksim.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kriuchkov.maksim.client.connection.MainService;

import java.io.IOException;

public class Client extends Application {

    static final String SERVER_IP_ADDRESS = "localhost";
    static final int SERVER_PORT = 8189;

    private static Scene scene;

    public Client() {
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
        scene = new Scene(fxmlLoader.load());
        MainWindowController controller = fxmlLoader.getController();
        controller.init();
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        MainService.getInstance().connect(SERVER_IP_ADDRESS, SERVER_PORT);
        launch();
    }
}
