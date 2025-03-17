package com.client.controller;

import com.client.model.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientHomeController {

    @FXML
    private TextField emailTextField;
    @FXML
    private Label newMessageLabel; // Campo per l'indirizzo email

    // Gestisce il login: verifica formato email, chiama il servizio Client e, se il login va a buon fine, carica la UI operativa
    @FXML
    private void handleLogin() {
        String email = emailTextField.getText();

        if (isValidEmail(email)) {
            // Chiamata al metodo centralizzato nella classe Client
            String response = Client.getInstance().login(email);

            if ("SUCCESSO: Login avvenuto con successo.".equals(response)) {
                ClientOperationController.setUserEmail(email);

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
                    Parent root = loader.load();

                    ClientOperationController controller = loader.getController();
                    controller.resetInboxSize();

                    Stage stage = (Stage) emailTextField.getScene().getWindow();
                    stage.setTitle("Mail Client - " + email);
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Errore", "Impossibile caricare la schermata successiva.");
                }
            } else {
                showError("Email non trovata", "L'indirizzo email inserito non è registrato nel nostro sistema.");
                emailTextField.clear();
            }
        } else {
            showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
            emailTextField.clear();
        }
    }

    // Verifica che l'email sia non nulla e rispetti il pattern definito
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    // Mostra un messaggio di errore in una finestra di dialogo
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Metodo chiamato in fase di chiusura dell'app
    public void shutdown() {
    }
}
