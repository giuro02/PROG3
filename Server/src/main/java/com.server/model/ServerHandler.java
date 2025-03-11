package com.server.model;

import com.common.Mail;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
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

            if ("LOGIN".equals(clientRequest)) {
                String email = (String) in.readObject();
                server.updateLogTable("Tentativo di login da: " + email);
                if (!server.isEmailRegistered(email)) {
                    out.writeObject("ERRORE: L'indirizzo email non esiste.");
                    server.updateLogTable("❌ Errore: L'email " + email + " non esiste.");
                } else {
                    out.writeObject("SUCCESSO: Login avvenuto con successo.");
                    server.updateLogTable("✅ Login riuscito per: " + email);
                }
                out.flush();
            } else if ("GET_INBOX".equals(clientRequest)) {
                String userEmail = (String) in.readObject();
                List<Mail> inbox = server.getInbox(userEmail);
                out.writeObject(inbox);
                out.flush();
            } else if ("SEND_MAIL".equals(clientRequest)) {
                Mail mail = (Mail) in.readObject();
                List<String> invalidRecipients = server.sendMail(mail);

                if (invalidRecipients.isEmpty()) {
                    out.writeObject("SUCCESSO");
                    server.updateLogTable("📩 Email inviata da " + mail.getSender() + " a: " + String.join(", ", mail.getReceiver()));
                } else {
                    out.writeObject("ERRORE: Il destinatario " + String.join(", ", invalidRecipients) + " non esiste");
                    server.updateLogTable("⚠ Errore nell'invio: Il destinatario " + String.join(", ", invalidRecipients) + " non esiste");
                }
                out.flush();
            } else if ("DELETE_MAIL".equals(clientRequest)) {
                String userEmail = (String) in.readObject();
                Mail mailToDelete = (Mail) in.readObject();
                boolean success = server.deleteMail(userEmail, mailToDelete);

                if (success) {
                    out.writeObject("SUCCESSO");
                    server.updateLogTable("Email eliminata per " + userEmail);
                } else {
                    out.writeObject("ERRORE: Email non trovata o eliminazione fallita.");
                    server.updateLogTable("Errore: impossibile eliminare email per " + userEmail);
                }
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            server.updateLogTable("Errore nella gestione della richiesta: " + e.getMessage());
        }
    }
}
