package com.server.controller;

import com.server.model.Server;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

/**
 * Controller per la UI del server.
 */
public class ServerController {
    @FXML
    private TextArea logArea;
    @FXML
    //private ListView<String> userList;

    private final Server server = Server.getInstance();

    @FXML
    public void initialize() {
        logArea.textProperty().bind(server.getLogTableProperty());
       // userList.setItems(server.getUsersProperty());
    }
     //viene usato per dire a JavaFX di aggiornare la GUI quando è pronto, senza bloccare il thread principale.
    public void appendLog(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }
}
