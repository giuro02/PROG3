package com.client.controller;

import com.client.model.Client;
import com.common.Mail;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private TextArea bodyTextField;

    // Gestisce l'invio della mail: raccoglie i dati, li valida e chiama il servizio Client per inviare l'email
    public void handleSend() {
        System.out.println("DEBUG: handleSend() called");

        String recipientLine = recipientTextField.getText();
        String subject = subjectTextField.getText();
        String body = bodyTextField.getText();

        if (recipientLine == null || recipientLine.trim().isEmpty()) {
            showError("Email non valida", "Per favore, inserisci almeno un indirizzo email valido.");
            return;
        }

        String[] recipientsArray = recipientLine.split("\\s*[;,]\\s*");
        List<String> recipients = new ArrayList<>();
        for (String r : recipientsArray) {
            String trimmed = r.trim();
            if (!isValidEmail(trimmed)) {
                showError("Email non valida", "Indirizzo non valido: " + trimmed);
                return;
            }
            recipients.add(trimmed);
        }

        String sender = ClientOperationController.getUserEmail();
        if (sender == null || sender.trim().isEmpty()) {
            showError("Errore", "L'email del mittente non è disponibile.");
            return;
        }

        int generatedId = new java.util.Random().nextInt(100000);
        Mail mail = new Mail(generatedId, subject, sender, new ArrayList<>(recipients), body, new Date());

        String response = Client.getInstance().sendMail(mail);
        if (!"SUCCESSO".equals(response)) {
            showError("Errore nell'invio", "Il server ha risposto: " + response);
            return;
        }
        System.out.println("DEBUG: Email inviata con successo.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
            Parent root = loader.load();

            ClientOperationController controller = loader.getController();
            controller.setUserEmail(sender);
            controller.updateInbox();
            controller.startAutoRefresh();

            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore", "Impossibile caricare l'interfaccia operativa.");
        }
    }

    // Precompila i campi della UI con i valori forniti (usato per reply, forward, ecc.)
    public void prefillFields(String recipients, String subject, String body) {
        recipientTextField.setText(recipients);
        subjectTextField.setText(subject);
        bodyTextField.setText(body);
    }

    // Gestisce il ritorno alla UI operativa
    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
            Parent root = loader.load();

            ClientOperationController controller = loader.getController();
            controller.setUserEmail(ClientOperationController.getUserEmail());
            controller.updateInbox();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore", "Impossibile caricare l'interfaccia operativa.");
        }
    }

    // Verifica che l'email sia sintatticamente valida
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
}
