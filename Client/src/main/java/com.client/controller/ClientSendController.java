// SendMessageController.java
package com.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;


import java.io.IOException;

public class ClientSendController {

    @FXML
    private Button sendButton;

    @FXML
    private Button backButton;

    @FXML
    private TextField recipientTextField;

    @FXML
    private TextField subjectTextField;

    @FXML
    private TextField bodyTextField;

    @FXML
    public void handleSend() {
        String recipient = recipientTextField.getText();
        String subject = subjectTextField.getText();
        String body = bodyTextField.getText();

        if (isValidEmail(recipient)) {
            System.out.println("Messaggio inviato a: " + recipient);
            recipientTextField.clear();
            subjectTextField.clear();
            bodyTextField.clear();
        } else {
            showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
        }
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/client/view/client-home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore di caricamento", "Impossibile tornare alla schermata principale.");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
