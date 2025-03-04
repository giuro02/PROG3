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

    private static String email;

    @FXML
    private ListView<String> list_mail;

    @FXML
    private TextArea mail_text;

    @FXML
    private Label mail_info;

    @FXML
    private Button replyButton, deleteButton;

    public static void setEmail(String userEmail) {
        email = userEmail;
    }

    public void setOperationController(String email, ClientApplication app, Client client) {
        this.client = client;
        this.email = email;
        this.app = app;
        this.selected_mail = null;

        response = new SimpleStringProperty("");

        response.bind(client.getResponse());
        list_mail.itemsProperty().bind(client.getUserMailsProperty());
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
        String selected_mail = list_mail.getSelectionModel().getSelectedItem();
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
        String selected_mail = list_mail.getSelectionModel().getSelectedItem();
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
    public void handleForward() {
        String selected_mail = list_mail.getSelectionModel().getSelectedItem();
        if (selected_mail != null) {
            mail_info.setText("Forwarding message...");
            mail_text.setText("Forward this message to: \n\n" + selected_mail);
        } else {
            showError("No message selected to forward.");
        }
    }

    @FXML
    public void handleDelete() {
        String selected_mail = list_mail.getSelectionModel().getSelectedItem();
        if (selected_mail != null) {
            list_mail.getItems().remove(selected_mail);
            mail_info.setText("Message deleted");
            mail_text.setText("The selected message has been deleted.");
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
