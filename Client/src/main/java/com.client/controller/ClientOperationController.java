package com.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.*;

public class ClientOperationController {

    private static String email;  // Variabile per memorizzare l'email dell'utente

    @FXML
    private ListView<String> inboxListView;  // Lista dei messaggi in arrivo

    @FXML
    private Button sendButton;  // Bottone per inviare un messaggio

    @FXML
    private TextField recipientTextField;  // Campo per inserire il destinatario

    @FXML
    private TextField subjectTextField;  // Campo per l'oggetto del messaggio

    @FXML
    private TextField bodyTextField;  // Campo per il corpo del messaggio

    @FXML
    private Button replyButton;  // Bottone per rispondere al messaggio

    @FXML
    private Button replyAllButton;  // Bottone per rispondere a tutti

    @FXML
    private Button forwardButton;  // Bottone per inoltrare il messaggio

    @FXML
    private Button deleteButton;  // Bottone per cancellare un messaggio

    // Metodo per impostare l'email dell'utente dopo il login
    public static void setEmail(String userEmail) {
        email = userEmail;
    }

    // Metodo per inviare un nuovo messaggio
    @FXML
    public void handleSend() {
        String recipient = recipientTextField.getText();
        String subject = subjectTextField.getText();
        String body = bodyTextField.getText();

        if (isValidEmail(recipient)) {
            // Qui va il codice per inviare l'email al server
            System.out.println("Messaggio inviato a: " + recipient);
            // Pulisci i campi
            recipientTextField.clear();
            subjectTextField.clear();
            bodyTextField.clear();
        } else {
            showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
        }
    }

    // Metodo per rispondere al messaggio selezionato
    @FXML
    public void handleReply() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            String sender = getSenderFromMessage(selectedMessage);
            recipientTextField.setText(sender);
            bodyTextField.setText("Risposta al messaggio...");
        }
    }

    // Metodo per rispondere a tutti i destinatari del messaggio selezionato
    @FXML
    public void handleReplyAll() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            List<String> recipients = getRecipientsFromMessage(selectedMessage);
            recipientTextField.setText(String.join(", ", recipients));
            bodyTextField.setText("Risposta a tutti...");
        }
    }

    // Metodo per inoltrare il messaggio selezionato
    @FXML
    public void handleForward() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            bodyTextField.setText("Inoltro del messaggio...");
        }
    }

    // Metodo per cancellare il messaggio selezionato
    @FXML
    public void handleDelete() {
        String selectedMessage = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            // Qui va il codice per cancellare il messaggio dal server
            System.out.println("Messaggio cancellato: " + selectedMessage);
            inboxListView.getItems().remove(selectedMessage);
        }
    }

    // Verifica se l'email è valida
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Mostra un messaggio di errore
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Estrai il mittente dal messaggio (formato delimitato)
    private String getSenderFromMessage(String message) {
        String[] parts = message.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("From:")) {
                return part.split(":")[1].trim();
            }
        }
        return null;
    }

    // Estrai i destinatari dal messaggio (formato delimitato)
    private List<String> getRecipientsFromMessage(String message) {
        List<String> recipients = new ArrayList<>();
        String[] parts = message.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("To:")) {
                String recipientsString = part.split(":")[1].trim();
                String[] recipientArray = recipientsString.split(", ");
                recipients.addAll(Arrays.asList(recipientArray));
                break;
            }
        }
        return recipients;
    }

}





/*package com.client.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import java.util.List;

public class ClientOperationController {

    @FXML
    private ListView<String> mailListView;  // Lista dei messaggi
    @FXML
    private TextArea mailDetailTextArea;  // Dettagli del messaggio selezionato
    @FXML
    private Button deleteButton;  // Bottone per cancellare un messaggio
    @FXML
    private Button replyButton;  // Bottone per rispondere al messaggio
    @FXML
    private Button forwardButton;  // Bottone per inoltrare il messaggio

    private List<String> mailList;  // Lista dei messaggi (esempio)

    // Carica la lista dei messaggi nella ListView
    public void loadMailList(List<String> mails) {
        mailList.clear();
        mailList.addAll(mails);
        mailListView.setItems((ObservableList<String>) mailList);
    }

    // Visualizza i dettagli del messaggio selezionato
    @FXML
    public void displayMailDetails() {
        String selectedMail = mailListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            // Ottieni i dettagli del messaggio (questo dipende dalla struttura del tuo modello)
            mailDetailTextArea.setText("Dettagli del messaggio: \n" + selectedMail);
        } else {
            showError("Seleziona un messaggio", "Per favore, seleziona un messaggio dalla lista.");
        }
    }

    // Cancella il messaggio selezionato
    @FXML
    public void deleteMail() {
        String selectedMail = mailListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            // Logica per eliminare il messaggio dal server
            mailList.remove(selectedMail);
            mailListView.getItems().remove(selectedMail);
            System.out.println("Messaggio eliminato: " + selectedMail);
        } else {
            showError("Seleziona un messaggio", "Per favore, seleziona un messaggio da eliminare.");
        }
    }

    // Rispondi al messaggio selezionato (solo al mittente)
    @FXML
    public void replyToMail() {
        String selectedMail = mailListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            // Logica per rispondere al mittente
            System.out.println("Risposta al messaggio: " + selectedMail);
        } else {
            showError("Seleziona un messaggio", "Per favore, seleziona un messaggio a cui rispondere.");
        }
    }

    // Inoltra il messaggio selezionato a nuovi destinatari
    @FXML
    public void forwardMail() {
        String selectedMail = mailListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            // Logica per inoltrare il messaggio
            System.out.println("Messaggio inoltrato: " + selectedMail);
        } else {
            showError("Seleziona un messaggio", "Per favore, seleziona un messaggio da inoltrare.");
        }
    }

    // Mostra un messaggio di errore
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}*/
