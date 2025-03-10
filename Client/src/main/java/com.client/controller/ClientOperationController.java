package com.client.controller;

import com.common.Mail;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientOperationController {

    private Mail selected_mail;

    // Usa un'unica variabile per l'email dell'utente
    private static String userEmail;

    @FXML
    private ListView<Mail> inboxListView;

    @FXML
    private TextArea mail_text;

    @FXML
    private Label mail_info;

    @FXML
    private Button replyButton, deleteButton, writeButton;

    @FXML
    private TextField destinatarioField;

    // Imposta l'email dell'utente (da chiamare dopo il login)
    public static void setUserEmail(String email) {
        userEmail = email;
    }

    public static String getUserEmail() {
        return userEmail;
    }
        //Platform.runLater() ensures that the GUI thread has fully loaded before calling updateInbox().
        //This avoids timing issues where userEmail might not be set when initialize() runs.
        @FXML
        public void initialize() {
            Platform.runLater(() -> {
                if (userEmail == null || userEmail.isEmpty()) {
                    userEmail = getUserEmail();  // Ensure email is set
                }
                if (userEmail != null && !userEmail.isEmpty()) {
                    updateInbox();  // Fetch inbox immediately
                }
            });
        }

    public void updateInbox() {
        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_INBOX");
            out.writeObject(userEmail);
            out.flush();

            List<Mail> newInbox = (List<Mail>) in.readObject();

            Platform.runLater(() -> {
                inboxListView.getItems().setAll(newInbox);  // Instead of clear() + addAll()
            });

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleButtonClick() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            mail_info.setText("Sending date: " + selectedMail.getDate());
            mail_text.setText(selectedMail.getMessage());
        } else {
            showError("No message selected.");
        }
    }

    @FXML
    public void handleReply() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            String sender = getSenderFromMessage(selectedMail);
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
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            List<String> recipients = getRecipientsFromMessage(selectedMail);
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
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            mail_info.setText("Forwarding message...");
            mail_text.setText("Forward this message to: \n\n" + selectedMail);
            String forwardTo = destinatarioField.getText();
            if (isValidEmail(forwardTo)) {
                mail_info.setText("Message forwarded to: " + forwardTo);
            } else {
                showError("Invalid email address.");
            }
        } else {
            showError("No message selected to forward.");
        }
    }

    @FXML
    public void handleDelete() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail == null) {
            showError("No message selected to delete.");
            return;
        }

        System.out.println("DEBUG: Attempting to delete mail: " + selectedMail);

        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("DELETE_MAIL");
            out.writeObject(ClientOperationController.getUserEmail());  // Send user email
            out.writeObject(selectedMail);  // Send mail to delete
            out.flush();

            String response = (String) in.readObject();
            if ("SUCCESSO".equals(response)) {
                inboxListView.getItems().remove(selectedMail);  // Remove from UI
                updateInbox();  // Refresh from server
                System.out.println("DEBUG: Successfully deleted from UI and server.");
            } else {
                showError("Failed to delete email.");
                System.out.println("DEBUG: Server responded with error: " + response);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showError("Error deleting email.");
        }
    }




    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getSenderFromMessage(Mail message) {
        String messageContent = message.getMessage();
        String[] parts = messageContent.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("From:")) {
                return part.split(":")[1].trim();
            }
        }
        return null;
    }

    private List<String> getRecipientsFromMessage(Mail message) {
        List<String> recipients = new ArrayList<>();
        String messageContent = message.getMessage();
        String[] parts = messageContent.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("To:")) {
                String recipientsString = part.split(":")[1].trim();
                recipients.addAll(Arrays.asList(recipientsString.split(",\\s*")));
                break;
            }
        }
        return recipients;
    }

    @FXML
    public void handleWrite() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-send.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) replyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }
}
