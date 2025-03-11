package com.client.controller;

import com.common.Mail;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientOperationController {

    // User's email (set after login)
    private static String userEmail;
    // To track the last inbox size for notification purposes
    private int lastInboxSize = 0;
    private Set<Integer> newMessageIds = new HashSet<>();
    @FXML
    private Label newMessageLabel;

    @FXML
    private ListView<Mail> inboxListView;
    @FXML
    private TextArea mail_text;
    @FXML
    private Label mail_info;
    @FXML
    private Button replyButton, replyAllButton, deleteButton, writeButton;
    @FXML
    private TextField destinatarioField;
    @FXML
    private Label notificationLabel;
    // Scheduled executor for polling the server for inbox updates
    private ScheduledExecutorService scheduler;

    // Set the user's email (called after login)
    public static void setUserEmail(String email) {
        userEmail = email;
    }
    public static String getUserEmail() {
        return userEmail;
    }

    // This method is automatically called after the FXML is loaded.
    @FXML
    public void initialize() {
        // Avvia il polling dell'inbox ogni 5 secondi
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (userEmail != null && !userEmail.isEmpty()) {
                updateInbox();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Contacts the server to fetch the inbox for the current user.
     * If new messages are detected, a notification is shown.
     */
    // Add a field at the top of the class
    private boolean initialInboxLoaded = false;
    @FXML
    public void updateInbox() {
        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_INBOX");
            out.writeObject(userEmail);
            out.flush();

            List<Mail> newInbox = (List<Mail>) in.readObject();

            Platform.runLater(() -> {
                // Se è il primo caricamento, non mostriamo notifica
                if (lastInboxSize == 0) {
                    lastInboxSize = newInbox.size();
                }

                // Se il numero di messaggi è aumentato, aggiungi gli ID dei nuovi messaggi
                if (newInbox.size() > lastInboxSize) {
                    for (Mail mail : newInbox) {
                        // Se non era già presente nella ListView, lo consideriamo nuovo
                        if (!inboxListView.getItems().contains(mail)) {
                            newMessageIds.add(mail.getId());
                        }
                    }
                    // Aggiorna la label dedicata
                    if (!newMessageIds.isEmpty()) {
                        newMessageLabel.setText("Hai " + newMessageIds.size() + " nuovi messaggi!");
                    }
                }

                // Aggiorna la ListView
                inboxListView.getItems().setAll(newInbox);
                lastInboxSize = newInbox.size();
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleButtonClick() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            // Se questo messaggio è tra i "nuovi"
            if (newMessageIds.contains(selectedMail.getId())) {
                newMessageIds.remove(selectedMail.getId());
                if (newMessageIds.isEmpty()) {
                    newMessageLabel.setText(""); // svuota la notifica
                } else {
                    newMessageLabel.setText("Hai " + newMessageIds.size() + " nuovi messaggi!");
                }
            }

            // Mostra i dettagli del messaggio nell'area di testo
            mail_info.setText("Sending date: " + selectedMail.getDate());
            String body = selectedMail.getMessage();
            if (selectedMail.getTitle() != null && selectedMail.getTitle().toLowerCase().startsWith("fwd:")) {
                body = "FORWARDED MESSAGE:\n\n" + body;
            }
            mail_text.setText(body);
        } else {
            showError("No message selected.");
        }
    }


    @FXML
    public void handleReply() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail == null) {
            showError("No message selected to reply.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-send.fxml"));
            Parent root = loader.load();

            ClientSendController sendController = loader.getController();

            // Get the original sender's email address
            String senderEmail = selectedMail.getSender();

            // Original subject
            String originalSubject = selectedMail.getTitle();

            // Create the new subject by prepending "Re:" to the original subject
            String newSubject = ensureReplyPrefix(originalSubject);

            // Leave the body empty for the user to type their response
            String newBody = "";

            // Fill the fields in the SendController
            sendController.prefillFields(senderEmail, newSubject, newBody);

            // Switch to the Send email scene
            Stage stage = (Stage) replyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    /**
     * Utility: if the subject is "MySubject", returns "Re: MySubject".
     * If it already starts with "Re:", leaves it alone.
     */
    private String ensureReplyPrefix(String subject) {
        if (subject == null) subject = "";
        subject = subject.trim();
        if (subject.toLowerCase().startsWith("re:")) {
            return subject;
        } else {
            return "Re: " + subject;
        }
    }


    //@FXML
    @FXML
    public void handleReplyAll() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail == null) {
            showError("No message selected to reply to all.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-send.fxml"));
            Parent root = loader.load();

            ClientSendController sendController = loader.getController();

            // Get the original sender's email address
            String senderEmail = selectedMail.getSender();

            // Get all recipients (including the original sender)
            List<String> allRecipients = new ArrayList<>(selectedMail.getReceiver());
            allRecipients.add(senderEmail);

            // Remove the sender's email from the list of recipients to avoid replying to oneself
            String userEmail = ClientOperationController.getUserEmail();
            allRecipients.remove(userEmail);  // Remove the current user's email if it's in the list

            // Create the new subject by prepending "Re:" to the original subject
            String originalSubject = selectedMail.getTitle();
            String newSubject = ensureReplyPrefix(originalSubject);

            // Leave the body empty for the user to type their response
            String newBody = "";

            // Fill the fields in the SendController
            sendController.prefillFields(String.join(", ", allRecipients), newSubject, newBody);

            // Switch to the Send email scene
            Stage stage = (Stage) replyAllButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    @FXML
    public void handleForward() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail == null) {
            showError("No message selected to forward.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-send.fxml"));
            Parent root = loader.load();
            ClientSendController sendController = loader.getController();

            // Pre-fill the send screen with the original email details
            String originalSubject = selectedMail.getTitle();
            String newSubject = originalSubject.toLowerCase().startsWith("fwd:") ? originalSubject : "Fwd: " + originalSubject;
            String newBody = selectedMail.getMessage();
            // Optionally, you can decide to prepend a "FORWARDED MESSAGE:" line to the displayed details,
            // but keep the stored body unchanged.
            sendController.prefillFields("", newSubject, newBody);

            Stage stage = (Stage) writeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    @FXML
    public void handleDelete() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail == null) {
            showError("No message selected to delete.");
            return;
        }

        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("DELETE_MAIL");
            out.writeObject(getUserEmail());
            out.writeObject(selectedMail);
            out.flush();

            String response = (String) in.readObject();
            if ("SUCCESSO".equals(response)) {
                updateInbox(); // Refresh inbox from the server
                mail_text.setText("");
                mail_info.setText("");
            } else {
                showError("Failed to delete email.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error deleting email.");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private String getSenderFromMessage(Mail message) {
        String[] parts = message.getMessage().split(";");
        for (String part : parts) {
            if (part.trim().toLowerCase().startsWith("from:")) {
                return part.split(":")[1].trim();
            }
        }
        return null;
    }

    private List<String> getRecipientsFromMessage(Mail message) {
        List<String> recipients = new ArrayList<>();
        String[] parts = message.getMessage().split(";");
        for (String part : parts) {
            if (part.trim().toLowerCase().startsWith("to:")) {
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

    // Shutdown the scheduler when the client application is closing
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public void startAutoRefresh() {
        // Start the scheduled executor if it's not already running.
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                if (userEmail != null && !userEmail.isEmpty()) {
                    updateInbox();
                }
            }, 0, 5, TimeUnit.SECONDS);
        }
    }

}
