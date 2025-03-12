package com.server.model;

import com.common.Mail;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                    threadPool.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (!running) break; // Exit loop when stopping server
                    server.updateLogTable("❌ Errore con un client: " + e.getMessage());
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
            String response = "UNKNOWN_COMMAND";  // Default response

            if ("LOGIN".equals(clientRequest)) {
                String email = (String) in.readObject();
                server.updateLogTable("Tentativo di login da: " + email);

                if (!server.isEmailRegistered(email)) {
                    response = "ERRORE: L'indirizzo email non esiste.";
                    server.updateLogTable("❌ Errore: L'email " + email + " non esiste.");
                } else {
                    response = "SUCCESSO: Login avvenuto con successo.";
                    server.updateLogTable("✅ Login riuscito per: " + email);
                }
            } else if ("GET_INBOX".equals(clientRequest)) {
                String userEmail = (String) in.readObject();
                // Ottieni la casella completa (tutti i messaggi)
                List<Mail> inbox = server.getInbox(userEmail);
                // Calcola il numero di messaggi non letti
                int unreadCount = server.getUnreadCount(userEmail);

                // Crea una Map per trasmettere i due valori
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("mails", inbox);
                responseMap.put("unreadCount", unreadCount);

                out.writeObject(responseMap);
                out.flush();
                return;
            } else if ("SEND_MAIL".equals(clientRequest)) {
                Mail mail = (Mail) in.readObject();
                List<String> invalidRecipients = server.sendMail(mail);

                if (invalidRecipients.isEmpty()) {
                    response = "SUCCESSO";
                    server.updateLogTable("📩 Email inviata da " + mail.getSender());
                } else {
                    response = "ERRORE: Il destinatario " + String.join(", ", invalidRecipients) + " non esiste";
                }
            } else if ("DELETE_MAIL".equals(clientRequest)) {
                String userEmail = (String) in.readObject();
                Mail mailToDelete = (Mail) in.readObject();
                boolean success = server.deleteMail(userEmail, mailToDelete);

                if (success) {
                    response = "SUCCESSO";
                    server.updateLogTable("Email eliminata per " + userEmail);
                } else {
                    response = "ERRORE: Email non trovata o eliminazione fallita.";
                }
            } else if ("MARK_READ".equals(clientRequest)) {
                // Gestione del comando per marcare un messaggio come letto
                String userEmail = (String) in.readObject();
                int mailId = (int) in.readObject();
                server.markMailAsRead(userEmail, mailId);
                response = "SUCCESSO";
            }

            out.writeObject(response);  // SEND FINAL RESPONSE
            out.flush();

        } catch (java.net.SocketException e) {
            // Se l'eccezione contiene "An established connection was aborted...",
            // non logghiamo nulla. Altrimenti, logghiamo come prima.
            String msg = e.getMessage();
            if (msg == null || !msg.contains("An established connection was aborted")) {
                server.updateLogTable("Errore nella gestione della richiesta: " + e.getMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            server.updateLogTable("Errore nella gestione della richiesta: " + e.getMessage());
        }
    }

}
