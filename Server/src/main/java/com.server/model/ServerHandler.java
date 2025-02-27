package com.server.model;
import com.common.Mail;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Server che gestisce le email con connessioni non permanenti.
 */
public class ServerHandler {

    private static final int PORT = 4000; // Porta del server
    private static final String LOG_FILE = "server_logs.txt"; // File di log

    /**
     * Avvia il server e rimane in ascolto di nuove connessioni.
     */
    public static void startServer() {
        log("🟢 Server avviato. In attesa di richieste...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) { //  Il server è sempre pronto ad accettare nuove richieste
                try (Socket clientSocket = serverSocket.accept()) { //  Accetta un client alla volta
                    log(" Nuovo client connesso: " + clientSocket.getInetAddress());
                    handleClient(clientSocket); //  Gestisce la richiesta e chiude la connessione
                } catch (IOException e) {
                    log("⚠ Errore con un client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log(" Errore nell'avvio del server: " + e.getMessage());
        }
    }

    /**
     * Gestisce una richiesta di un client.
     */
    private static void handleClient(Socket clientSocket) {
        try (
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            String clientRequest = (String) in.readObject();
            log(" Richiesta ricevuta: " + clientRequest);

            if (clientRequest.startsWith("GET_MAILS")) {
                String userEmail = clientRequest.split(":")[1]; // Ottiene l'email del client
                List<Mail> emails = Server.getInstance().getInbox(userEmail); // Ottiene le email dal server
                out.writeObject(emails); //  Invia solo i nuovi messaggi, senza trasferire tutta la casella
                log("📨 Inviate " + emails.size() + " email a " + userEmail);
            }

        } catch (IOException | ClassNotFoundException e) {
            log(" Errore nella gestione della richiesta: " + e.getMessage());
        }
    }

    /**
     * Registra un messaggio nei log.
     */
    private static void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        String fullMessage = "[" + timestamp + "] " + message;

        System.out.println(fullMessage); // Stampa in console

        // Salva nel file di log
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            bw.write(fullMessage + "\n");
        } catch (IOException e) {
            System.out.println("⚠ Errore nel salvataggio dei log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        startServer(); // Avvia il server
    }
}
