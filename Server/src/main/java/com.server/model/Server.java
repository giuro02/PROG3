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

        // 5) Se tutti i destinatari erano validi, ritorna "SUCCESSO", altrimenti solo i destinatari invalidi
        return invalidRecipients.isEmpty() ? Collections.emptyList() : invalidRecipients;
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

    // Getter e metodi di log/utenti

    public SimpleStringProperty getLogTableProperty() {
        return logTable;
    }

    public ObservableList<String> getUsersProperty() {
        return users;
    }

    public void updateLogTable(String message) {
        Platform.runLater(() -> {
            if (!logTable.get().contains(message)) {
                logTable.set(logTable.get() + "\n" + message);
            }
        });
    }

    public void addUser(String user) {
        Platform.runLater(() -> users.add(user));
    }

    public void removeUser(String user) {
        Platform.runLater(() -> users.remove(user));
    }
}
