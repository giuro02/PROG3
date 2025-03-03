package com.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class ClientOperationController {

    private static String email;
    public Text emailInfoTitle;
    public TextArea emailInfoTextArea;

    @FXML
    private ListView<String> inboxListView;

    @FXML
    private Button replyButton;
    @FXML
    private Button replyAllButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button deleteButton;

    public static void setEmail(String userEmail) {
        email = userEmail;
    }

    //DA GESTIREEEEE
    @FXML
    public void onButtonClicked() {
        // Logica per quando si clicca un elemento nella lista
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            // Puoi fare qualcosa con il messaggio selezionato
            emailInfoTitle.setText("Message: " + selectedMessage);
            emailInfoTextArea.setText("Content of the message...");
        } else {
            showError("No message selected.");
        }
    }

    @FXML
    public void handleReply() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            String sender = getSenderFromMessage(selectedMessage);
            if (sender != null) {
                emailInfoTitle.setText("Reply to: " + sender);
                emailInfoTextArea.setText("Replying to " + sender + "\n\n" + "Type your response here...");
            } else {
                showError("Sender information not found.");
            }
        }
    }

    @FXML
    public void handleReplyAll() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            List<String> recipients = getRecipientsFromMessage(selectedMessage);
            if (!recipients.isEmpty()) {
                emailInfoTitle.setText("Replying to all: " + String.join(", ", recipients));
                emailInfoTextArea.setText("Replying to all recipients...\n\n" + "Type your response here...");
            } else {
                showError("Recipients information not found.");
            }
        }
    }

    @FXML
    public void handleForward() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            emailInfoTitle.setText("Forwarding message...");
            emailInfoTextArea.setText("Forward this message to: \n\n" + selectedMessage);
        } else {
            showError("No message selected to forward.");
        }
    }

    @FXML
    public void handleDelete() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            inboxListView.getItems().remove(selectedMessage);
            emailInfoTitle.setText("Message deleted");
            emailInfoTextArea.setText("The selected message has been deleted.");
        } else {
            showError("No message selected to delete.");
        }
    }

    private String getSenderFromMessage(String message) {
        String[] parts = message.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("From:")) {
                return part.split(":")[1].trim();
            }
        }
        return null;
    }

    private List<String> getRecipientsFromMessage(String message) {
        List<String> recipients = new ArrayList<>();
        String[] parts = message.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("To:")) {
                String recipientsString = part.split(":")[1].trim();
                recipients.addAll(Arrays.asList(recipientsString.split(", ")));
                break;
            }
        }
        return recipients;
    }

    @FXML
    public void onWriteButtonClick() {
        try {
            // Carica il file FXML per la nuova finestra
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/client/view/client-send.fxml"));
            Parent root = loader.load();

            // Ottieni la scena corrente
            Stage stage = (Stage) replyButton.getScene().getWindow();

            // Cambia la scena
            stage.setScene(new Scene(root));

            // Mostra la nuova finestra
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
