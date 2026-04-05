package hr.javafx.onlinelearningsystem.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SceneManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SceneManager.class);
    private static final double INITIAL_SCENE_WIDTH = 1200;
    private static final double INITIAL_SCENE_HEIGHT = 800;
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void showScene(String fxmlFile){
        try{
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
            Parent root = loader.load();

            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, INITIAL_SCENE_WIDTH, INITIAL_SCENE_HEIGHT);
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.setTitle("Online Learning System");
            primaryStage.show();

        } catch (IOException e) {
            LOGGER.error("Error loading scene FXML: {}", fxmlFile, e);
        } catch (NullPointerException e) {
            LOGGER.error("Scene FXML file not found: {}", fxmlFile, e);
        }
    }


    public static void showPopup(String fxmlFile, Window owner) {
        try{
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Online Learning System");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(owner);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();

        } catch (IOException e) {
            LOGGER.error("Error loading popup FXML: {}", fxmlFile, e);
        } catch (NullPointerException e) {
            LOGGER.error("Popup FXML file not found: {}", fxmlFile, e);
        }
    }

    public static void closeDialog(Window owner) {
        Stage stage = (Stage) owner;
        stage.close();
    }

    private SceneManager() {}
}