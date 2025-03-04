package com.client.controller;

import com.client.ClientApplication;
import com.client.model.Client;
import com.common.Mail;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientOperationController {

    private Mail selected_mail;

    private static String email;

    @FXML
    private ListView<Mail> list_mail;

    @FXML
    private TextArea mail_text;

    @FXML
    private Label mail_info;

    @FXML
    private Button replyButton, deleteButton;

    public static void setEmail(String userEmail) {
        email = userEmail;
    }

    @FXML
    public void handleButtonClick() {
        Mail selected_mail = list_mail.getSelectionModel().getSelectedItem();
        if (selected_mail != null) {

            mail_info.setText("Sending date: " + selected_mail.getDate());
            mail_text.setText(selected_mail.getMessage());
        } else {
            showError("No message selected.");
        }
    }

    @FXML
    public void handleReply() {
        Mail selected_mail = list_mail.getSelectionModel().getSelectedItem();
        if (selected_mail != null) {
            String sender = getSenderFromMessage(selected_mail);
            if (sender != null) {
                mail_info.setText("Reply to: " + sender);
                mail_text.setText("Replying to " + sender + "\n\nType your response here...");
            } else {
                showError("Sender information not found.");
            }
        }
    }

    @FXML
    public void handleReplyAll() {
        Mail selected_mail = list_mail.getSelectionModel().getSelectedItem();
        if (selected_mail != null) {
            List<String> recipients = getRecipientsFromMessage(selected_mail);
            if (!recipients.isEmpty()) {
                mail_info.setText("Replying to all: " + String.join(", ", recipients));
                mail_text.setText("Replying to all recipients...\n\nType your response here...");
            } else {
                showError("Recipients information not found.");
            }
        }
    }

    @FXML
    private TextField destinatarioField; // Campo per inserire l'indirizzo email del destinatario

    @FXML
    public void handleForward() {
        // Ottieni la mail selezionata
        Mail selectedMail = list_mail.getSelectionModel().getSelectedItem();

        if (selectedMail != null) {
            // Mostra il messaggio di forwarding
            mail_info.setText("Forwarding message...");

            // Mostra il testo della mail che verrà inoltrata
            mail_text.setText("Forward this message to: \n\n" + selectedMail);

            // A questo punto possiamo chiedere all'utente di inserire un destinatario
            String forwardTo = destinatarioField.getText(); // supponiamo che il campo del destinatario sia "destinatarioField"

            // Verifica se l'email è valida
            if (isValidEmail(forwardTo)) {
                // Invia la mail o fai qualsiasi altro passo necessario per il forwarding
                mail_info.setText("Message forwarded to: " + forwardTo);
            } else {
                // Mostra un errore se l'email non è valida
                showError("Invalid email address.");
            }

        } else {
            // Mostra un errore se non è stata selezionata nessuna mail
            showError("No message selected to forward.");
        }
    }


    @FXML
    public void handleDelete() {
        Mail selected_mail = list_mail.getSelectionModel().getSelectedItem();
        if (selected_mail != null) {
            list_mail.getItems().remove(selected_mail);
            mail_info.setText("Message deleted");
            mail_text.setText("The selected message has been deleted.");
        } else {
            showError("No message selected to delete.");
        }
    }

    private boolean isValidEmail(String email) {
        // Verifica se l'email è sintatticamente corretta usando una regex
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }


    // Metodo per ottenere il mittente da un messaggio
    private String getSenderFromMessage(Mail message) {
        // Otteniamo il contenuto del messaggio come stringa
        String messageContent = message.getMessage();

        // Dividiamo il contenuto in base al punto e virgola
        String[] parts = messageContent.split(";");

        // Scorriamo le parti per cercare la riga che inizia con "From:"
        for (String part : parts) {
            if (part.trim().startsWith("From:")) {
                // Restituiamo l'indirizzo del mittente, rimuovendo eventuali spazi
                return part.split(":")[1].trim();
            }
        }
        // Se non troviamo il mittente, restituiamo null
        return null;
    }

    // Metodo per ottenere i destinatari da un messaggio
    private List<String> getRecipientsFromMessage(Mail message) {
        List<String> recipients = new ArrayList<>();

        // Otteniamo il contenuto del messaggio come stringa
        String messageContent = message.getMessage();

        // Dividiamo il contenuto in base al punto e virgola
        String[] parts = messageContent.split(";");

        // Scorriamo le parti per cercare la riga che inizia con "To:"
        for (String part : parts) {
            if (part.trim().startsWith("To:")) {
                // Estraiamo i destinatari dalla riga, separandoli per virgola e spazio
                String recipientsString = part.split(":")[1].trim();
                recipients.addAll(Arrays.asList(recipientsString.split(",\\s*"))); // Utilizziamo regex per gestire spazi opzionali
                break;
            }
        }

        // Restituiamo la lista dei destinatari
        return recipients;
    }


    @FXML
    public void handleWrite() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/client/view/client-send.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) replyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
