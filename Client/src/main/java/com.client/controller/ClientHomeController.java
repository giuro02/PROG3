package com.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.application.Platform;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHomeController {

    @FXML
    private TextField emailTextField;  // Campo per inserire l'indirizzo email

    //@FXML
    //private Button loginButton;  // Bottone per inviare l'indirizzo email e autenticarsi

    // Metodo per gestire il login con l'indirizzo email


    @FXML
    private void handleLogin() {
        String email;

        while (true) {
            email = emailTextField.getText();

            if (isValidEmail(email)) {
                String response = sendLoginRequestToServer(email);

                if ("SUCCESSO: Login avvenuto con successo.".equals(response)) {
                    System.out.println("Autenticazione riuscita con l'email: " + email);
                    ClientOperationController.setUserEmail(email);

                    startAutoRefresh();  // Avvia il thread di aggiornamento automatico

                    // Load the new scene for the operations
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
                        Parent root = loader.load();

                        // 1) Get the current stage
                        Stage stage = (Stage) emailTextField.getScene().getWindow();
                        // 2) Set the window title with the user’s email
                        stage.setTitle("Mail Client - " + email);

                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Errore", "Impossibile caricare la schermata successiva.");
                    }
                    break;
                } else {
                    showError("Email non trovata", "L'indirizzo email inserito non è registrato nel nostro sistema.");
                    emailTextField.clear();
                    return;
                }
            } else {
                showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
                emailTextField.clear();
                return;
            }
        }
    }

    private void startAutoRefresh() {
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



    private String sendLoginRequestToServer(String email) {
        try (Socket socket = new Socket("localhost", 4000);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Invia la richiesta di login al server
            out.writeObject("LOGIN");
            out.writeObject(email);
            out.flush();

            // Ricevi la risposta dal server
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showError("Errore di connessione", "Impossibile connettersi al server.");
            return null;
        }
    }


    private boolean isValidEmail(String email) {
        // Verifica se l'email è sintatticamente corretta usando una regex
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }



    // Mostra un messaggio di errore
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

