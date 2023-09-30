package kriuchkov.maksim.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import kriuchkov.maksim.client.connection.MainService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainWindowController {

    private MainService mainService = MainService.getInstance();

    public TextField loginTextField;
    public PasswordField passwordField;
    public Button loginButton;
    public Button deleteButton;
    public Button fetchButton;
    public Button storeButton;
    public ListView<String> remoteFolderListView;
    public ListView<String> localFolderListView;

    private List<Control> fileControls;
    private List<Control> loginControls;

    private final Path localFolder = Paths.get("local");

    private final Runnable storeSuccess = () -> Platform.runLater( () ->
    {
        try {
            updateLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    private final Consumer<String> storeFailure = (msg) -> {
        Platform.runLater( () ->
                showErrorAlert(msg));
        Platform.runLater( () ->
        {
            try {
                updateLists();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

    private final Runnable fetchSuccess = () -> Platform.runLater( () ->
    {
        try {
            updateLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    private final Consumer<String> fetchFailure = (msg) -> {
        Platform.runLater( () ->
                showErrorAlert(msg));
        Platform.runLater( () ->
        {
            try {
                updateLists();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

    private final Runnable deleteSuccess = () -> Platform.runLater( () ->
    {
        try {
            updateLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    private final Consumer<String> deleteFailure = (msg) -> {
        Platform.runLater( () ->
                showErrorAlert(msg));
        Platform.runLater( () ->
        {
            try {
                updateLists();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

    private final Runnable authSuccess = () -> Platform.runLater( () ->
    {
        enableFileControls();
        disableLoginControls();
        try {
            updateLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    private final Consumer<String> authFailure = (msg) -> {
        disableFileControls();
        enableLoginControls();
        Platform.runLater( () ->
                showErrorAlert(msg));
    };

//    private final Runnable updateSuccess = () -> {
//
//    };
//
//    private final Consumer<String> updateFailure = () -> {
//
//    };
    
    @FXML
    private void storeButtonPress() throws Exception {
        String fileName = localFolderListView.getSelectionModel().getSelectedItem();
        if (fileName == null || fileName.isEmpty())
            return;
        mainService.store(fileName, storeSuccess, storeFailure);
    }

    @FXML
    private void fetchButtonPress() throws IOException {
        String fileName = remoteFolderListView.getSelectionModel().getSelectedItem();
        if (fileName == null || fileName.isEmpty())
            return;
        mainService.fetch(fileName, fetchSuccess, fetchFailure);
    }

    void init() throws IOException {
        fileControls = new ArrayList<>();
        fileControls.add(deleteButton);
        fileControls.add(fetchButton);
        fileControls.add(storeButton);
        fileControls.add(remoteFolderListView);
        fileControls.add(localFolderListView);

        loginControls = new ArrayList<>();
        loginControls.add(loginTextField);
        loginControls.add(passwordField);
        loginControls.add(loginButton);

        if (!MainService.getInstance().getAuthorized()) {
            disableFileControls();
        }
    }

    private void disableFileControls() {
        Platform.runLater( () -> {
            for (Control c : fileControls) {
                c.setDisable(true);
            }
        });
    }

    private void enableFileControls() {
        Platform.runLater( () -> {
            for (Control c : fileControls) {
                c.setDisable(false);
            }
        });
    }

    private void disableLoginControls() {
        Platform.runLater( () -> {
            for (Control c : loginControls) {
                c.setDisable(true);
            }
        });
    }

    private void enableLoginControls() {
        Platform.runLater( () -> {
            for (Control c : loginControls) {
                c.setDisable(false);
            }
        });
    }

    void updateLists() throws IOException {
        // получить списки файлов

        // на сервере
        mainService.list( (list) ->
                Platform.runLater( () ->
                        remoteFolderListView.setItems(FXCollections.observableList(list))));

        // на клиенте
        if (Files.notExists(localFolder)) {
            Files.createDirectory(localFolder);
        }
        List<String> list = Files.list(localFolder)
                .filter(Files::isRegularFile)
                .map(path -> path.getName(path.getNameCount() - 1).toString())
                .collect(Collectors.toList());
        Platform.runLater( () ->
                localFolderListView.setItems(FXCollections.observableList(list)) );
    }

    private void showErrorAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.show();
    }

    private void showWarningAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }

    @FXML
    private void deleteButtonPress() {
        String fileName = remoteFolderListView.getSelectionModel().getSelectedItem();
        if (fileName == null || fileName.isEmpty())
            return;
        mainService.delete(fileName, deleteSuccess, deleteFailure);
    }

    @FXML
    private void loginButtonPress() {
        String login = loginTextField.getText();
        if (login.isEmpty()) {
            showWarningAlert("Enter login");
            return;
        }
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showWarningAlert("Enter password");
            return;
        }
        mainService.auth(login, password, authSuccess, authFailure);
    }
}
