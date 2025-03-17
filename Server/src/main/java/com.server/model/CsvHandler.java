package com.server.model;

import com.common.Mail;
import java.io.*;
import java.util.*;

public class CsvHandler {

    // Carica gli account dal file data.csv
    public static Set<String> loadAccounts(String filePath) {
        Set<String> accounts = new HashSet<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File " + filePath + " not found. No accounts loaded.");
            return accounts;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    accounts.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    // Carica le email dal file emails.csv e popola le mailbox
    public static Map<String, List<Mail>> loadMailboxes(String filePath) {
        Map<String, List<Mail>> mailboxes = new HashMap<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File " + filePath + " not found. No emails loaded.");
            return mailboxes;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            readFromBufferedReader(mailboxes, br);
        } catch (IOException e) {
            System.out.println("Error loading emails: " + e.getMessage());
        }
        return mailboxes;
    }

    // Metodo ausiliario per leggere le mail dal BufferedReader, ripristinando eventuali newline
    private static void readFromBufferedReader(Map<String, List<Mail>> mailboxes, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                int id = Integer.parseInt(parts[0].trim());
                String sender = parts[1].trim();
                String receiverField = parts[2].trim();
                String title = parts[3].trim();
                String message = parts[4].trim();
                // Ripristina le newline sostituite durante la scrittura
                String restoredMessage = message.replace("\\n", "\n").replace("\\r", "\r");
                Date date = new Date(Long.parseLong(parts[5].trim()));

                // Dividi i destinatari
                ArrayList<String> recipients = new ArrayList<>(Arrays.asList(receiverField.split(";")));

                // Crea l'oggetto Mail
                Mail mail = new Mail(id, title, sender, recipients, restoredMessage, date);
                // Aggiunge la mail nella mailbox di ciascun destinatario
                for (String r : recipients) {
                    r = r.trim();
                    mailboxes.putIfAbsent(r, new ArrayList<>());
                    mailboxes.get(r).add(mail);
                }
            } else {
                System.out.println("Malformed line, ignored: " + line);
            }
        }
    }

    // Salva le mailbox nel file emails.csv, sanitizzando i messaggi per mantenere la riga unica
    public static void saveMailboxes(Map<String, List<Mail>> mailboxes, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (Map.Entry<String, List<Mail>> entry : mailboxes.entrySet()) {
                String recipient = entry.getKey();
                for (Mail mail : entry.getValue()) {
                    String originalMessage = mail.getMessage();
                    // Sostituisce i newline con sequenze innocue
                    String sanitizedMessage = originalMessage
                            .replace("\n", "\\n")
                            .replace("\r", "\\r");

                    bw.write(mail.getId() + ","
                            + mail.getSender().trim() + ","
                            + recipient.trim() + ","
                            + mail.getTitle().trim() + ","
                            + sanitizedMessage + ","
                            + mail.getDate().getTime());
                    bw.newLine();
                }
            }
            System.out.println("DEBUG: Saved updated mailboxes to file.");
        } catch (IOException e) {
            System.out.println(" Error saving emails: " + e.getMessage());
        }
    }
}
