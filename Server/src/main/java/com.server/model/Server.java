// MODEL: Server.java
/*package com.server.model;

import com.common.Mail;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

public class Server {
    private static Server instance;
    private final Map<String, List<Mail>> mailboxes;
    private final SimpleStringProperty logTable;
    private final ObservableList<String> users;

    private Server() {
        mailboxes = CsvHandler.loadMailboxes();
        logTable = new SimpleStringProperty("");
        users = FXCollections.observableArrayList();

    }
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }


    public synchronized List<String> sendMail(Mail mail) {
        if (!isEmailRegistered(mail.getSender())) {
            return List.of("ERRORE: Il mittente non esiste");
        }

        List<String> invalidRecipients = new ArrayList<>();
        for (String recipient : mail.getReceiver()) {
            if (!mailboxes.containsKey(recipient)) {
                invalidRecipients.add(recipient);
                System.out.println("invalid" + recipient);

            } else {
                mailboxes.get(recipient).add(mail);
            }
        }
        CsvHandler.saveMailboxes(mailboxes);
        updateLogTable("Nuova email inviata da " + mail.getSender());
        return invalidRecipients.isEmpty() ? List.of("SUCCESSO") : invalidRecipients;
    }

    public synchronized List<Mail> getInbox(String userEmail) {
        return mailboxes.getOrDefault(userEmail, new ArrayList<>());
    }

    public boolean isEmailRegistered(String email) {
        System.out.println("Checking email: " + email);
        File file = new File("data.csv");
        System.out.println("Looking for file at: " + file.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Read line: [" + line + "]");
                if (line.trim().equalsIgnoreCase(email.trim())) {
                    System.out.println("Email FOUND: " + email);
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Email NOT found: " + email);
        return false;
    }



    /*public boolean isEmailRegistered(String email) {
      //  return mailboxes.containsKey(email); // Controlla se l'email esiste nella mappa mailboxes
    //}

    public SimpleStringProperty getLogTableProperty() { return logTable; }
    public ObservableList<String> getUsersProperty() { return users; }
    public void updateLogTable(String message) { Platform.runLater(() -> logTable.set(logTable.get() + "\n" + message)); }
    public void addUser(String user) { Platform.runLater(() -> users.add(user)); }
    public void removeUser(String user) { Platform.runLater(() -> users.remove(user)); }
}*/
package com.server.model;

import com.common.Mail;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class Server {
    private static Server instance;

    // Set di account validi (le email registrate), lette da data.csv
    private final Set<String> accounts;

    // Mappa delle caselle di posta: chiave = indirizzo email, valore = lista di Mail
    private final Map<String, List<Mail>> mailboxes;

    private final SimpleStringProperty logTable;
    private final ObservableList<String> users;

    // Percorsi dei file (puoi cambiare i nomi come preferisci)
    private static final String ACCOUNTS_FILE = "data.csv";   // per gli indirizzi validi
    private static final String MAILS_FILE = "emails.csv";    // per i messaggi salvati

    /**
     * Costruttore privato: legge accounts e mailboxes dai due file separati
     */
    private Server() {
        // 1) Carica gli account da data.csv
        accounts = CsvHandler.loadAccounts(ACCOUNTS_FILE);

        // 2) Carica le mail da emails.csv
        mailboxes = CsvHandler.loadMailboxes(MAILS_FILE);

        // Inizializza le strutture di log e utenti con JavaFX
        logTable = new SimpleStringProperty("");
        users = FXCollections.observableArrayList();
    }

    /**
     * Singleton: restituisce l'istanza unica di Server
     */
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    /**
     * Invio di una mail: controlla che il mittente esista, poi aggiunge la mail
     * alle inbox dei destinatari validi, e salva su emails.csv
     */
    public synchronized List<String> sendMail(Mail mail) {
        // 1) Controlla se il mittente è registrato
        if (!isEmailRegistered(mail.getSender())) {
            return List.of("ERRORE: Il mittente non esiste");
        }

        // 2) Per ogni destinatario, controlla se esiste. Se non esiste, lo metti in invalidRecipients
        List<String> invalidRecipients = new ArrayList<>();
        for (String recipient : mail.getReceiver()) {
            if (!isEmailRegistered(recipient)) {
                invalidRecipients.add(recipient);
            } else {
                // Se la mailbox non esiste ancora, la creiamo
                mailboxes.putIfAbsent(recipient, new ArrayList<>());
                mailboxes.get(recipient).add(mail);
            }
        }

        // 3) Salva le caselle di posta aggiornate su emails.csv
        CsvHandler.saveMailboxes(mailboxes, MAILS_FILE);

        // 4) Logga l'evento
        updateLogTable("Nuova email inviata da " + mail.getSender());

        // 5) Se tutti i destinatari erano validi, ritorna SUCCESSO, altrimenti i destinatari invalidi
        return invalidRecipients.isEmpty() ? List.of("SUCCESSO") : invalidRecipients;
    }

    /**
     * Restituisce la inbox (lista di Mail) per un certo utente
     */
    public synchronized List<Mail> getInbox(String userEmail) {
        return mailboxes.getOrDefault(userEmail, new ArrayList<>());
    }

    /**
     * Verifica se un email è registrata tra gli account (data.csv),
     * senza più leggere fisicamente dal file ogni volta
     */
    public boolean isEmailRegistered(String email) {
        return accounts.contains(email);
    }

    // Getter e metodi di log/utenti

    public SimpleStringProperty getLogTableProperty() {
        return logTable;
    }

    public ObservableList<String> getUsersProperty() {
        return users;
    }

    public void updateLogTable(String message) {
        Platform.runLater(() -> logTable.set(logTable.get() + "\n" + message));
    }

    public void addUser(String user) {
        Platform.runLater(() -> users.add(user));
    }

    public void removeUser(String user) {
        Platform.runLater(() -> users.remove(user));
    }
}
