package com.server.model;

import com.common.Mail;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.*;

public class Server {
    private static Server instance;
    private Map<String, Set<Integer>> readMap = new HashMap<>();
    private final Set<String> accounts;
    private final Map<String, List<Mail>> mailboxes;
    private final SimpleStringProperty logTable;
    private final ObservableList<String> users; //lista osservabile di utenti per aggiornare automaticamente la UI

    private static final String ACCOUNTS_FILE = "data.csv";
    private static final String MAILS_FILE = "emails.csv";

    // Costruttore privato: carica accounts e mailbox dai file e inizializza log e utenti
    private Server() {
        accounts = CsvHandler.loadAccounts(ACCOUNTS_FILE);
        mailboxes = CsvHandler.loadMailboxes(MAILS_FILE);
        loadReadMail();
        logTable = new SimpleStringProperty("");
        users = FXCollections.observableArrayList();
    }

    // Restituisce l'istanza unica di Server (Singleton)
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    // Gestisce l'invio di una mail: controlla il mittente, aggiunge la mail alle mailbox dei destinatari validi e salva su file
    public synchronized List<String> sendMail(Mail mail) {
        if (!isEmailRegistered(mail.getSender())) {
            return List.of("ERRORE: Il mittente non esiste");
        }
        List<String> invalidRecipients = new ArrayList<>();
        for (String recipient : mail.getReceiver()) {
            if (!isEmailRegistered(recipient)) {
                invalidRecipients.add(recipient);
            } else {
                mailboxes.putIfAbsent(recipient, new ArrayList<>());
                mailboxes.get(recipient).add(mail);
            }
        }
        CsvHandler.saveMailboxes(mailboxes, MAILS_FILE);
        updateLogTable("Nuova email inviata da " + mail.getSender());
        return invalidRecipients.isEmpty() ? Collections.emptyList() : invalidRecipients;
    }

    // Restituisce la mailbox per un determinato utente
    public synchronized List<Mail> getInbox(String userEmail) {
        return mailboxes.getOrDefault(userEmail, new ArrayList<>());
    }

    // Verifica se un'email è registrata
    public boolean isEmailRegistered(String email) {
        return accounts.contains(email);
    }

    // Elimina una mail dalla mailbox di un utente e salva le modifiche su file
    public synchronized boolean deleteMail(String userEmail, Mail mailToDelete) {
        if (!mailboxes.containsKey(userEmail)) {
            return false;
        }
        List<Mail> userInbox = mailboxes.get(userEmail);
        boolean removed = userInbox.removeIf(mail -> mail.equals(mailToDelete));
        if (removed) {
            CsvHandler.saveMailboxes(mailboxes, "emails.csv");
            Platform.runLater(() -> updateLogTable("Email eliminata per " + userEmail));
        }
        return removed;
    }

    // Restituisce la proprietà del log (per il binding con la UI)
    public SimpleStringProperty getLogTableProperty() {
        return logTable;
    }

    // Aggiunge un messaggio al log, aggiornando la UI in modo thread-safe
    public void updateLogTable(String message) {
        Platform.runLater(() -> {
            if (!logTable.get().contains(message)) {
                logTable.set(logTable.get() + "\n" + message);
            }
        });
    }

    // Carica le letture (read Mail) da file
    private void loadReadMail() {
        File file = new File("readMail.csv");
        if (!file.exists()) {
            System.out.println("File readMail.csv non trovato. Nessuna lettura caricata.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String email = parts[0].trim();
                    int mailId = Integer.parseInt(parts[1].trim());
                    readMap.putIfAbsent(email, new HashSet<>());
                    readMap.get(email).add(mailId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Salva le letture (read Mail) su file
    private void saveReadMail() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("readMail.csv", false))) {
            for (Map.Entry<String, Set<Integer>> entry : readMap.entrySet()) {
                String email = entry.getKey();
                for (Integer mailId : entry.getValue()) {
                    bw.write(email + "," + mailId);
                    bw.newLine();
                }
            }
            System.out.println("DEBUG: Read Mail salvati.");
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio di read Mail: " + e.getMessage());
        }
    }

    // Marca una mail come letta per un utente e salva il read receipt su file
    public synchronized void markMailAsRead(String userEmail, int mailId) {
        readMap.putIfAbsent(userEmail, new HashSet<>());
        readMap.get(userEmail).add(mailId);
        saveReadMail();  // Salva subito le modifiche
    }

    // Calcola il numero di messaggi non letti per un utente
    public synchronized int getUnreadCount(String userEmail) {
        List<Mail> allMails = mailboxes.getOrDefault(userEmail, new ArrayList<>());
        Set<Integer> readIds = readMap.getOrDefault(userEmail, new HashSet<>());
        int count = 0;
        for (Mail mail : allMails) {
            if (!readIds.contains(mail.getId())) {
                count++;
            }
        }
        return count;
    }
}
