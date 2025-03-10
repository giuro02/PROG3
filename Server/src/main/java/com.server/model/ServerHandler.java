package com.server.model;

import com.common.Mail;
import javafx.scene.control.Alert;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestisce le connessioni dei client.
 */
public class ServerHandler {

    private static final int PORT = 4000;
    private static ServerSocket serverSocket;
    private static final Server server = Server.getInstance();
    private static boolean running = true; // Flag to stop server
    private static final ExecutorService threadPool = Executors.newCachedThreadPool(); // Manages client connections

    public static void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setReuseAddress(true);  // Important to allow reusing the port after closing
            server.updateLogTable(" Server started on port: " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    server.updateLogTable(" Nuovo client connesso: " + clientSocket.getInetAddress());

                    // Start handling client in a separate thread
                    threadPool.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (!running) break; // Exit loop when stopping server
                    server.updateLogTable(" Errore con un client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            server.updateLogTable(" Errore nell'avvio del server: " + e.getMessage());
        }
    }

    public static void stopServer() {
        running = false;  // Set flag to stop the server

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close the ServerSocket
            }
            threadPool.shutdown(); // Stop accepting new connections
            server.updateLogTable(" Server stopped.");
        } catch (IOException e) {
            server.updateLogTable("⚠ Errore durante la chiusura del server: " + e.getMessage());
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
                String email = (String) in.readObject();
                if (!server.isEmailRegistered(email)) {
                    out.writeObject("ERRORE: L'indirizzo email non esiste.");
                } else {
                    out.writeObject("SUCCESSO: Login avvenuto con successo.");
                }
                out.flush();
            } else if ("SEND_MAIL".equals(clientRequest)) {
                Mail mail = (Mail) in.readObject();
                List<String> result = server.sendMail(mail);
                if (result.contains("SUCCESSO")) {
                    out.writeObject("SUCCESSO");
                } else {
                    out.writeObject("ERRORE: Destinatari non validi: " + result);
                }
                out.flush();
            }
            // Get inbox request
            else if ("GET_INBOX".equals(clientRequest)) {
                String userEmail = (String) in.readObject();
                List<Mail> inbox = server.getInbox(userEmail);
                out.writeObject(inbox);
                out.flush();
            } else {
                server.updateLogTable(" Comando non riconosciuto: " + clientRequest);
            }
             if ("DELETE_MAIL".equals(clientRequest)) {
                String userEmail = (String) in.readObject();  // Read user email
                Mail mailToDelete = (Mail) in.readObject();  // Read the mail object to delete

                boolean success = server.deleteMail(userEmail, mailToDelete);
                if (success) {
                    out.writeObject("SUCCESSO");
                } else {
                    out.writeObject("ERRORE: Email not found or deletion failed.");
                }
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            server.updateLogTable(" Errore nella gestione della richiesta: " + e.getMessage());
        }
    }
}
