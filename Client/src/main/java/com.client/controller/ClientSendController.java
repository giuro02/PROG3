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
    private TextField bodyTextField;

    @FXML
    public void handleSend() {
        System.out.println("DEBUG: handleSend() called");

        // Retrieve the values from the text fields.
        String recipient = recipientTextField.getText();
        String subject = subjectTextField.getText();
        String body = bodyTextField.getText();

        System.out.println("DEBUG: recipientTextField = [" + recipient + "]");
// Validate the recipient email.
        if (!isValidEmail(recipient)) {
            showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
            return;
        }

        // Retrieve the sender's email from ClientOperationController.
        String sender = ClientOperationController.getUserEmail();
        if (sender == null || sender.trim().isEmpty()) {
            showError("Errore", "L'email del mittente non è disponibile.");
            return;
        }

        // Create a Mail object using the input values.
        Mail mail = new Mail(0, subject, sender, new ArrayList<>(List.of(recipient)), body, new Date());

        // Send the email to the server over a socket.
        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send a command to the server indicating we want to send an email.
            out.writeObject("SEND_MAIL");
            // Send the Mail object.
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

        // After successfully sending the email, switch back to the operations interface.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
            Parent root = loader.load();

            ClientOperationController controller = loader.getController();
            // Se il controller ha un metodo setEmail(...) o setUserEmail(...), puoi passargli la mail dell'utente
            controller.setUserEmail(sender);
            // Aggiorna la inbox
            controller.updateInbox();

            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore", "Impossibile caricare l'interfaccia operativa.");
        }
    }


    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore di caricamento", "Impossibile tornare alla schermata principale.");
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
