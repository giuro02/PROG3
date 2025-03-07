// MODEL: Server.java
package com.server.model;

import com.common.Mail;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        return mailboxes.containsKey(email); // Controlla se l'email esiste nella mappa mailboxes
    }*/

    public SimpleStringProperty getLogTableProperty() { return logTable; }
    public ObservableList<String> getUsersProperty() { return users; }
    public void updateLogTable(String message) { Platform.runLater(() -> logTable.set(logTable.get() + "\n" + message)); }
    public void addUser(String user) { Platform.runLater(() -> users.add(user)); }
    public void removeUser(String user) { Platform.runLater(() -> users.remove(user)); }
}
