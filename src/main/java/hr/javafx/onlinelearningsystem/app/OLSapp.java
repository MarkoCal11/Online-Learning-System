package hr.javafx.onlinelearningsystem.app;

import hr.javafx.onlinelearningsystem.exception.DatabaseInitializationException;
import hr.javafx.onlinelearningsystem.util.AlertUtil;
import hr.javafx.onlinelearningsystem.util.DbInitializer;
import hr.javafx.onlinelearningsystem.util.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Objects;

public class OLSapp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(OLSapp.class);

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(80, 80);

        Label loadingLabel = new Label("Starting application...");
        loadingLabel.getStyleClass().add("login-label");

        VBox loadingBox = new VBox(16, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);

        StackPane splashRoot = new StackPane(loadingBox);

        splashRoot.getStylesheets().add(
                Objects.requireNonNull(OLSapp.class.getResource("/hr/javafx/onlinelearningsystem/styles.css")).toExternalForm()
        );

        stage.setScene(new Scene(splashRoot, 1200, 800));

        stage.setTitle("Online Learning System");
        stage.show();

        Thread.startVirtualThread(() -> {
            try {
                DbInitializer.initIfNeeded();
                Platform.runLater(() ->
                        SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/Login.fxml")
                );
            } catch (DatabaseInitializationException e) {
                LOGGER.error("Startup failed", e);
                Platform.runLater(() -> {
                        AlertUtil.showError("Database initialization failed.");
                        Platform.exit();
                });
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}