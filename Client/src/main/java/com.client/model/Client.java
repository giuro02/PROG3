package com.client.model;

import com.common.Mail;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern; //TOGLILO SE NON LO USIAMO PIUUUUUUU

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4000;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userEmail;
    private ObservableList<Mail> inbox;

    @FXML
    private TextField emailField;
    @FXML
    private ListView<Mail> inboxListView;
    @FXML
    private Button sendButton;

    public Client() {
        inbox = FXCollections.observableArrayList();
    }

    // Connessione al server
    public void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connessione al server avvenuta con successo.");
        } catch (IOException e) {
            showError("Errore di connessione", "Impossibile connettersi al server.");
        }
    }

    // Verifica della sintassi dell'indirizzo email
    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex); //NON USIAMO IL PATTERN SEMBRA TROPPO COPIATOOOOO
        return pattern.matcher(email).matches(); //LEVAAAAAAAA
    }

    // Metodo per l'autenticazione e la connessione con il server
    public void authenticateUser() {
        boolean emailIsValid = false;

        // Continua a chiedere l'email finché non è valida
        while (!emailIsValid) {
            userEmail = emailField.getText(); // Ottieni l'email inserita dall'utente

            // Verifica che l'email sia valida
            if (!isValidEmail(userEmail)) {
                // Mostra un errore e chiedi all'utente di inserirla di nuovo
                showError("Indirizzo Email Non Valido", "Inserisci un indirizzo email valido.");
                return; // Interrompe il processo finché l'email non è valida
            } else {
                emailIsValid = true; // L'email è valida, possiamo procedere
            }
        }

        // Connettersi al server prima di inviare l'autenticazione
        connectToServer(); // Assicurati che la connessione venga stabilita

        try {
            // Invia la richiesta di autenticazione al server con l'email
            out.writeObject("AUTH " + userEmail);
            out.flush();
            String response = (String) in.readObject(); // Aspetta la risposta dal server

            // Se la risposta è "SUCCESSO", l'utente è autenticato
            if ("SUCCESSO".equals(response)) {
                updateInbox(); // Recupera i messaggi della casella in arrivo
                System.out.println("Autenticazione avvenuta con successo.");
            } else {
                showError("Autenticazione Fallita", "Indirizzo email non trovato.");
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Errore di comunicazione", "Errore durante la comunicazione con il server.");
        }
    }



    // Metodo per aggiornare la lista dei messaggi ricevuti (Inbox)
    public void updateInbox() {
        try {
            out.writeObject("GET_INBOX " + userEmail);
            out.flush(); //invia subito i dati al server svuotanto il buffer.
            ArrayList<Mail> messages = (ArrayList<Mail>) in.readObject();

            Platform.runLater(() -> inbox.setAll(messages));
            inboxListView.setItems(inbox);
        } catch (IOException | ClassNotFoundException e) {
            showError("Errore di aggiornamento", "Impossibile recuperare i messaggi.");
        }
    }

    // Invia un messaggio al server
    /*public void sendMail() {
        try {
            String recipient = "destinatario@example.com";  // Esempio, va ottenuto tramite l'interfaccia utente
            String subject = "Oggetto del messaggio";
            String messageText = "Contenuto del messaggio";

            Mail mail = new Mail(1, subject, userEmail, new ArrayList<String>() {{
                add(recipient);
            }}, messageText, new Date());

            out.writeObject("SEND_MAIL");
            out.writeObject(mail);
            out.flush();

            String response = (String) in.readObject();
            if ("SUCCESSO".equals(response)) {
                System.out.println("Messaggio inviato con successo.");
            } else {
                showError("Errore nell'invio", "Impossibile inviare il messaggio.");
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Errore di invio", "Errore durante l'invio del messaggio.");
        }
    }*/

    // Metodo per cancellare un messaggio dalla Inbox
    /*public void deleteMessage(Mail mail) {
        try {
            out.writeObject("DELETE_MAIL");
            out.writeObject(mail);
            out.flush();

            String response = (String) in.readObject();
            if ("SUCCESSO".equals(response)) {
                inbox.remove(mail);
            } else {
                showError("Errore", "Impossibile cancellare il messaggio.");
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Errore", "Errore durante la cancellazione del messaggio.");
        }
    }*/

    // Visualizza i dettagli di un messaggio
    /*public void showMailDetails(MouseEvent event) {
        Mail selectedMail = inboxListView.getSelectionModel().getSelectedItem();
        if (selectedMail != null) {
            String details = "Da: " + selectedMail.getSender() + "\n" +
                    "Oggetto: " + selectedMail.getTitle() + "\n" +
                    "Messaggio: " + selectedMail.getMessage();
            System.out.println(details);
        }
    }*/

    // Gestisce gli errori e mostra un messaggio all'utente
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
