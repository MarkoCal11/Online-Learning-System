package hr.javafx.onlinelearningsystem.app;

import hr.javafx.onlinelearningsystem.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class OLSapp extends Application {


    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);
        SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/Login.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
