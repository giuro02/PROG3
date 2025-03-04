// MODEL: Server.java
package com.server.model;

import com.common.Mail;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;

public class Server {
    private static Server instance;
    private final Map<String, List<Mail>> mailboxes;
    private final SimpleStringProperty logTable;
    private final ObservableList<String> users;

    private Server() {
        mailboxes = new HashMap<>();
        logTable = new SimpleStringProperty("");
        users = FXCollections.observableArrayList();
        CsvHandler.loadMailboxes();
        createDefaultAccounts();
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    private void createDefaultAccounts() {
        String[] defaultUsers = {"senda@esame.it", "giulia@esame.it", "liliana@esame.it"};
        for (String user : defaultUsers) {
            mailboxes.putIfAbsent(user, new ArrayList<>());
        }
        CsvHandler.saveMailboxes(mailboxes);
    }

    public synchronized List<String> sendMail(Mail mail) {
        if (!mailboxes.containsKey(mail.getSender())) {
            return List.of("ERRORE: Il mittente non esiste");
        }

        List<String> invalidRecipients = new ArrayList<>();
        for (String recipient : mail.getReceiver()) {
            if (!mailboxes.containsKey(recipient)) {
                invalidRecipients.add(recipient);
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
        return mailboxes.containsKey(email); // Controlla se l'email esiste nella mappa mailboxes
    }

    public SimpleStringProperty getLogTableProperty() { return logTable; }
    public ObservableList<String> getUsersProperty() { return users; }
    public void updateLogTable(String message) { Platform.runLater(() -> logTable.set(logTable.get() + "\n" + message)); }
    public void addUser(String user) { Platform.runLater(() -> users.add(user)); }
    public void removeUser(String user) { Platform.runLater(() -> users.remove(user)); }
}
