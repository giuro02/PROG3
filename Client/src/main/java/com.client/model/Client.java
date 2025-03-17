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
import java.util.Map;
import java.util.regex.Pattern; //TOGLILO SE NON LO USIAMO PIUUUUUUU

/*public class Client {

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

    private boolean isValidEmail(String email) {
        // Verifica se l'email è sintatticamente corretta usando una regex
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
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
}*/

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4000;

    // Pattern Singleton per avere un'unica istanza condivisa
    private static Client instance;

    private Client() { }

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    // Metodo per effettuare il login (sostituisce la logica ora presente in ClientHomeController)
    public String login(String email) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LOGIN");
            out.writeObject(email);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metodo per ottenere la inbox, centralizzando GET_INBOX
    public Map<String, Object> getInbox(String userEmail) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_INBOX");
            out.writeObject(userEmail);
            out.flush();
            return (Map<String, Object>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metodo per inviare una mail (centralizza SEND_MAIL)
    public String sendMail(Mail mail) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SEND_MAIL");
            out.writeObject(mail);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metodo per cancellare una mail (centralizza DELETE_MAIL)
    public String deleteMail(String userEmail, Mail mail) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("DELETE_MAIL");
            out.writeObject(userEmail);
            out.writeObject(mail);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metodo per marcare una mail come letta (centralizza MARK_READ)
    public String markMailAsRead(String userEmail, int mailId) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("MARK_READ");
            out.writeObject(userEmail);
            out.writeObject(mailId);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
