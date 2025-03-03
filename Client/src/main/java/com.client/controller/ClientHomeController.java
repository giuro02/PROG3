package com.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class ClientHomeController {

    @FXML
    private TextField emailTextField;  // Campo per inserire l'indirizzo email

    //@FXML
    //private Button loginButton;  // Bottone per inviare l'indirizzo email e autenticarsi

    // Metodo per gestire il login con l'indirizzo email
    @FXML
    public void handleLogin() {
        String email;

        // Continuo a chiedere fino a quando l'email non è valida
        while (true) {
            email = emailTextField.getText();

            if (isValidEmail(email)) {
                // Se l'email è valida, procedo con l'autenticazione e la connessione al server
                // Qui va il codice per autenticare l'utente e passare alla schermata successiva
                System.out.println("Autenticazione riuscita con l'email: " + email);
                // Passa al controller delle operazioni
                ClientOperationController.setEmail(email);
                break; // Esco dal ciclo perché l'email è valida
            } else {
                // Mostra errore se l'email non è valida e chiede un nuovo tentativo
                showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
                emailTextField.clear(); // Pulisce il campo email per il nuovo tentativo
                return; // Torna alla stessa schermata e aspetta un nuovo inserimento
            }
        }
    }

    // Verifica la sintassi dell'email (puoi migliorarlo con espressioni regolari)
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
}





/*package com.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class ClientHomeController {

    @FXML
    private TextField emailTextField;  // Campo per inserire l'indirizzo email

    @FXML
    private Button loginButton;  // Bottone per inviare l'indirizzo email e autenticarsi

    // Metodo per gestire il login con l'indirizzo email
    @FXML
    public void handleLogin() {
        String email;

        // Continuo a chiedere fino a quando l'email non è valida
        while (true) {
            email = emailTextField.getText();

            if (isValidEmail(email)) {
                // Se l'email è valida, procedo con l'autenticazione e la connessione al server
                // Qui va il codice per autenticare l'utente e passare alla schermata successiva
                System.out.println("Autenticazione riuscita con l'email: " + email);
                break; // Esco dal ciclo perché l'email è valida
            } else {
                // Mostra errore se l'email non è valida e chiede un nuovo tentativo
                showError("Email non valida", "Per favore, inserisci un indirizzo email valido.");
                emailTextField.clear(); // Pulisce il campo email per il nuovo tentativo
                return; // Torna alla stessa schermata e aspetta un nuovo inserimento
            }
        }
    }


    // Verifica la sintassi dell'email (puoi migliorarlo con espressioni regolari)
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
}*/
