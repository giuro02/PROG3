package com.server.model;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestisce le connessioni dei client.
 */
public class ServerHandler {

    private static final int PORT = 4000;
    private static final Server server = Server.getInstance();

    public static void startServer() {
        server.updateLogTable(" Server avviato. In attesa di richieste...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
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
        } catch (IOException | ClassNotFoundException e) {
            server.updateLogTable(" Errore nella gestione della richiesta: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        Server.getInstance(); // Inizializza il server
        System.out.println("Server avviato con successo!");
    }

}
