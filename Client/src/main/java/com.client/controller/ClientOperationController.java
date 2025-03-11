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
    private Button replyButton, replyAllButton, deleteButton, writeButton;

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

            // The actual stored body
            String actualBody = selectedMail.getMessage();

            // If the subject starts with "Fwd:",
            // we dynamically prepend "FORWARDED MESSAGE:\n\n"
            // just for display:
            String displayedBody = actualBody;
            if (selectedMail.getTitle() != null
                    && selectedMail.getTitle().toLowerCase().startsWith("fwd:"))
            {
                displayedBody = "FORWARDED MESSAGE:\n\n" + displayedBody;
            }

            // Now show it in the detail area
            mail_text.setText(displayedBody);
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

            // Original subject & body
            String originalSubject = selectedMail.getTitle();
            String originalBody    = selectedMail.getMessage();

            // Add "Fwd:" only if not present
            String newSubject = ensureForwardPrefix(originalSubject);

            // For the body we do NOT add "FORWARDED MESSAGE" (store it plain):
            // This means the actual stored email body remains "impossibile" (for example).
            String newBody = originalBody;

            // Fill the fields in the SendController
            sendController.prefillFields(/* recipients */ "",
                    /* subject    */ newSubject,
                    /* body       */ newBody);

            Stage stage = (Stage) replyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    /**
     * Utility: if the subject is "MySubject", returns "Fwd: MySubject".
     * If it already starts with "Fwd:", leaves it alone.
     */
    private String ensureForwardPrefix(String subject) {
        if (subject == null) subject = "";
        subject = subject.trim();
        if (subject.toLowerCase().startsWith("fwd:")) {
            return subject;
        } else {
            return "Fwd: " + subject;
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
                mail_text.setText("");
                mail_info.setText("");
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

    void startAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    String nuoviMessaggi = controllaNuoviMessaggi();

                    if (nuoviMessaggi != null && !nuoviMessaggi.isEmpty()) {
                        Platform.runLater(() -> {
                            mostraNotifica("Nuovi messaggi ricevuti!");
                        });
                    }

                    Thread.sleep(5000); // Controlla ogni 5 secondi
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    private String controllaNuoviMessaggi() {
        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_NEW_MESSAGES");
            out.writeObject(ClientOperationController.getUserEmail()); // Usa l'email dell'utente
            out.flush();

            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostraNotifica(String messaggio) {
        System.out.println("Notifica: " + messaggio);
        // Qui puoi usare una finestra di dialogo, icona di sistema, ecc.
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
