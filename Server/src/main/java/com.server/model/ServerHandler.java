package com.server.model;

import com.common.Mail;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Gestisce le connessioni dei client.
 */


public class ServerHandler {

    private static TextField emailTextField;  // Campo per inserire l'indirizzo email

    private static final int PORT = 4000;
    private static final Server server = Server.getInstance();

    public static void startServer() {
        server.updateLogTable(" Server avviato. In attesa di richieste...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server started on port: " + PORT);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {

                    server.updateLogTable(" Nuovo client connesso: " + clientSocket.getInetAddress());
                    handleClient(clientSocket);
                } catch (IOException e) {
                    server.updateLogTable(" Errore con un client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            server.updateLogTable(" Errore nell'avvio del server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            String clientRequest = (String) in.readObject();
            server.updateLogTable("Richiesta ricevuta: " + clientRequest);

            if ("LOGIN".equals(clientRequest)) {
                // Leggi l'email e la password inviate dal client
                String email = (String) in.readObject();

                // Verifica che l'email esista
                if (!server.isEmailRegistered(email)) {
                    out.writeObject("ERRORE: L'indirizzo email non esiste.");
                } else {
                    // Login riuscito
                    out.writeObject("SUCCESSO: Login avvenuto con successo.");
                }
                out.flush();
            } else if ("SEND_MAIL".equals(clientRequest)) {
                // Leggi l'oggetto Mail dal client
                Mail mail = (Mail) in.readObject();

                // Invio della mail tramite il metodo sendMail del Server
                List<String> result = server.sendMail(mail);

                // Rispondi al client in base al risultato dell'invio
                if (result.contains("SUCCESSO")) {
                    out.writeObject("SUCCESSO");
                } else {
                    out.writeObject("ERRORE: Destinatari non validi: " + result);
                }
                out.flush();
            } else {
                server.updateLogTable("Comando non riconosciuto: " + clientRequest);
            }
        } catch (IOException | ClassNotFoundException e) {
            server.updateLogTable("Errore nella gestione della richiesta: " + e.getMessage());
        }
    }

    // Mostra un messaggio di errore
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void main(String[] args) {
        Server.getInstance(); // Inizializza il server
        System.out.println("Server avviato con successo!");
        startServer();
    }

}
