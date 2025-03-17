package com.client.controller;

import com.client.model.Client;
import com.common.Mail;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientOperationController {

    private static String userEmail;
    private int lastInboxSize = 0;
    private Set<Integer> unreadMessageIds = new HashSet<>();

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

    private ScheduledExecutorService scheduler;

    // Imposta l'email dell'utente
    public static void setUserEmail(String email) {
        userEmail = email;
    }

    // Restituisce l'email dell'utente
    public static String getUserEmail() {
        return userEmail;
    }

    // Inizializza il controller: pulisce notifiche e avvia il polling periodico per aggiornare l'inbox
    @FXML
    public void initialize() {
        unreadMessageIds.clear();
        updateNotificationLabel();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (userEmail != null && !userEmail.isEmpty()) {
                updateInbox(); //ogni 5 sec chiama updateInbox
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    // Recupera la inbox chiamando il servizio Client e aggiorna la UI con i messaggi e il conteggio dei non letti
    public void updateInbox() {
        Map<String, Object> responseMap = Client.getInstance().getInbox(userEmail);
        if (responseMap != null) {
            List<Mail> inbox = (List<Mail>) responseMap.get("mails");
            int unreadCount = (int) responseMap.get("unreadCount");

            Platform.runLater(() -> {
                ObservableList<Mail> currentItems = inboxListView.getItems();
                // Rimuove i messaggi che non sono più presenti nella lista ricevuta dal server
                currentItems.removeIf(mail -> !inbox.contains(mail));
                // Aggiunge i messaggi nuovi che non sono già presenti
                for (Mail mail : inbox) {
                    if (!currentItems.contains(mail)) {
                        currentItems.add(mail);
                    }
                }
                // Aggiorna l'etichetta delle notifiche
                if (unreadCount > 0) {
                    newMessageLabel.setText("New " + unreadCount + " messages!");
                } else {
                    newMessageLabel.setText("");
                }
            });

        }
    }


    /*public void updateInbox() {
    Map<String, Object> responseMap = Client.getInstance().getInbox(userEmail);
    if (responseMap != null) {
        List<Mail> inbox = (List<Mail>) responseMap.get("mails");
        int unreadCount = (int) responseMap.get("unreadCount");

        Platform.runLater(() -> {
            // Ottieni la lista attuale dei messaggi
            ObservableList<Mail> currentItems = inboxListView.getItems();
            // Per ogni messaggio ricevuto, se non è già presente, aggiungilo
            for (Mail mail : inbox) {
                if (!currentItems.contains(mail)) {
                    currentItems.add(mail);
                }
            }
            // Aggiorna l'etichetta delle notifiche (qui puoi anche riflettere solo il numero dei nuovi messaggi, se lo preferisci)
            if (unreadCount > 0) {
                newMessageLabel.setText("New " + unreadCount + " messages!");
            } else {
                newMessageLabel.setText("");
            }
        });
    }
}
*/

    // Resetta il contatore dell'inbox (usato in fase di login)
    public void resetInboxSize() {
        lastInboxSize = 0;
    }

    // Aggiorna l'etichetta delle notifiche in base al numero di messaggi non letti
    private void updateNotificationLabel() {
        if (!unreadMessageIds.isEmpty()) {
            newMessageLabel.setText("New " + unreadMessageIds.size() + " messages!");
        } else {
            newMessageLabel.setText("");
        }
    }

    // Gestisce il click su un messaggio: visualizza i dettagli e chiama la marcatura come letto
    @FXML
    public void handleButtonClick() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            mail_info.setText("Sending date: " + selectedMail.getDate());
            mail_text.setText(selectedMail.getMessage());
            markMessageAsRead(selectedMail);
        }
    }

    // Avvia un thread per marcare il messaggio come letto chiamando il servizio Client
    private void markMessageAsRead(Mail mail) {
        new Thread(() -> {
            String response = Client.getInstance().markMailAsRead(userEmail, mail.getId());
            if (!"SUCCESSO".equals(response)) {
                System.err.println("Errore nel marcare il messaggio come letto: " + response);
            }
        }).start();
    }

    // Gestisce l'azione di reply: apre la finestra per scrivere la risposta
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
            String senderEmail = selectedMail.getSender();
            String originalSubject = selectedMail.getTitle();
            String newSubject = ensureReplyPrefix(originalSubject);
            String newBody = "";

            sendController.prefillFields(senderEmail, newSubject, newBody);

            Stage stage = (Stage) replyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    // Aggiunge "Re:" all'oggetto se non già presente
    private String ensureReplyPrefix(String subject) {
        if (subject == null) subject = "";
        subject = subject.trim();
        if (subject.toLowerCase().startsWith("re:")) {
            return subject;
        } else {
            return "Re: " + subject;
        }
    }

    // Gestisce il reply-all: prepara la lista di destinatari e apre la UI per scrivere la risposta
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
            String senderEmail = selectedMail.getSender();
            List<String> allRecipients = new ArrayList<>(selectedMail.getReceiver());
            allRecipients.add(senderEmail);
            String userEmail = ClientOperationController.getUserEmail();
            allRecipients.remove(userEmail);
            String originalSubject = selectedMail.getTitle();
            String newSubject = ensureReplyPrefix(originalSubject);
            String newBody = "";

            sendController.prefillFields(String.join(", ", allRecipients), newSubject, newBody);

            Stage stage = (Stage) replyAllButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    // Gestisce l'azione di forward: apre la finestra per inoltrare il messaggio selezionato
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

            String originalSubject = selectedMail.getTitle();
            String newSubject = originalSubject.toLowerCase().startsWith("fwd:") ? originalSubject : "Fwd: " + originalSubject;
            String newBody = selectedMail.getMessage();
            sendController.prefillFields("", newSubject, newBody);

            Stage stage = (Stage) replyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load the write email window.");
        }
    }

    // Gestisce la cancellazione di un messaggio: chiama il servizio Client e aggiorna l'inbox se l'operazione ha successo
    @FXML
    public void handleDelete() {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail == null) {
            showError("No message selected to delete.");
            return;
        }
        String response = Client.getInstance().deleteMail(userEmail, selectedMail);
        if ("SUCCESSO".equals(response)) {
            updateInbox();
            mail_text.setText("");
            mail_info.setText("");
        } else {
            showError("Failed to delete email.");
        }
    }

    // Gestisce l'apertura della finestra per scrivere una nuova mail
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

    // Avvia il polling automatico per l'aggiornamento dell'inbox, se il scheduler non è attivo
    public void startAutoRefresh() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                if (userEmail != null && !userEmail.isEmpty()) {
                    updateInbox();
                }
            }, 0, 5, TimeUnit.SECONDS);
        }
    }

    // Mostra un messaggio di errore in un dialogo, eseguito in modo thread-safe
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
