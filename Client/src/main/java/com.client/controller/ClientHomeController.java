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

        // Continuo a chiedere fino a quando l'email non è valida
        while (true) {
            email = emailTextField.getText();

            // Verifica che l'email sia sintatticamente corretta
            if (isValidEmail(email)) {
                // Se l'email è valida, procedo con l'autenticazione
                String response = sendLoginRequestToServer(email);

                if ("SUCCESSO: Login avvenuto con successo.".equals(response)) {
                    System.out.println("Autenticazione riuscita con l'email: " + email);
                    ClientOperationController.setUserEmail(email);

                    // Carica la nuova schermata per le operazioni
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-operation.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) emailTextField.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Errore", "Impossibile caricare la schermata successiva.");
                    }
                    break; // Esco dal ciclo perché l'email è valida e autenticata
                } else {
                    // Se l'email non è stata trovata sul server
                    showError("Email non trovata", "L'indirizzo email inserito non è registrato nel nostro sistema.");
                    emailTextField.clear();
                    return; // Torna alla stessa schermata e aspetta un nuovo inserimento
                }
            } else {
                // Mostra errore se l'email non è valida sintatticamente
                showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
                emailTextField.clear();
                return; // Torna alla stessa schermata e aspetta un nuovo inserimento
            }
        }
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

