// SendMessageController.java
package com.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import com.common.Mail;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.*;

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
    public void handleSend() {
        System.out.println("DEBUG: handleSend() called");

        // Retrieve the values from the text fields.
        String recipientLine = recipientTextField.getText();  // e.g., "alice@ex.com; bob@ex.com"
        String subject = subjectTextField.getText();
        String body = bodyTextField.getText();

        if (recipientLine == null || recipientLine.trim().isEmpty()) {
            showError("Email non valida", "Per favore, inserisci almeno un indirizzo email valido.");
            return;
        }

        // Split on comma or semicolon (with optional spaces) to support multiple recipients.
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

        // Retrieve the sender's email from ClientOperationController.
        String sender = ClientOperationController.getUserEmail();
        if (sender == null || sender.trim().isEmpty()) {
            showError("Errore", "L'email del mittente non è disponibile.");
            return;
        }

        // Generate a unique ID for the email.
        int generatedId = new java.util.Random().nextInt(100000);
        // Create a Mail object using the input values.
        Mail mail = new Mail(generatedId, subject, sender, new ArrayList<>(recipients), body, new Date());

        // Send the email to the server over a socket.
        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SEND_MAIL");
            out.writeObject(mail);
            out.flush();

            // Read the server's response.
            String response = (String) in.readObject();
            if (!"SUCCESSO".equals(response)) {
                showError("Errore nell'invio", "Il server ha risposto: " + response);
                return;
            }
            System.out.println("DEBUG: Email inviata con successo.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showError("Errore", "Si è verificato un errore durante l'invio dell'email.");
            return;
        }

        // After sending the email successfully, switch back to the operations interface.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
            Parent root = loader.load();

            ClientOperationController controller = loader.getController();
            // Pass the userEmail again if needed
            controller.setUserEmail(sender);
            controller.updateInbox();
            controller.startAutoRefresh(); // <-- Aggiungi questa riga per avviare l'aggiornamento automatico

            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore", "Impossibile caricare l'interfaccia operativa.");
        }
    }


    public void prefillFields(String recipients, String subject, String body) {
        recipientTextField.setText(recipients);
        subjectTextField.setText(subject);
        bodyTextField.setText(body);
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
            Parent root = loader.load();

            ClientOperationController controller = loader.getController();
            controller.setUserEmail(ClientOperationController.getUserEmail());  // Ensure email is set
            controller.updateInbox();  // Refresh inbox when coming back

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore", "Impossibile caricare l'interfaccia operativa.");
        }
    }



    private boolean isValidEmail(String email) {
        // Verifica se l'email è sintatticamente corretta usando una regex
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
