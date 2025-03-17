package com.server.controller;

import com.server.model.Server;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServerController {
    @FXML
    private TextArea logArea;
    // Istanza del server
    private final Server server = Server.getInstance();

    // Inizializza il controller legando la proprietà log del server all'area di testo della UI
    @FXML
    public void initialize() {
        logArea.textProperty().bind(server.getLogTableProperty());
    }
}
