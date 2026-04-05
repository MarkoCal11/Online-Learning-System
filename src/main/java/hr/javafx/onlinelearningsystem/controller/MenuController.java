package hr.javafx.onlinelearningsystem.controller;

import hr.javafx.onlinelearningsystem.util.SceneManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MenuController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MenuController.class);

    protected ScrollPane contentArea;
    protected MenuController(ScrollPane contentArea) {
        this.contentArea = contentArea;
    }

    protected void logout() {
        SceneManager.showScene("/hr/javafx/onlinelearningsystem/fxml/Login.fxml");
    }

    protected void loadSection(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node section = loader.load();
            contentArea.setContent(section);
        } catch (IOException e) {
            LOGGER.error("Failed to load section FXML: {}", fxmlFile, e);
        }
    }
}