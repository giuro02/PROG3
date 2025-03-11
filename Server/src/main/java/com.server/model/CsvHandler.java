package com.server.model;

import com.common.Mail;
import java.io.*;
import java.util.*;

public class CsvHandler {
    // File paths:
    // - data.csv for accounts (one email per line)
    // - emails.csv for emails (6 columns: id, sender, receiver, title, message, date)
    private static final String ACCOUNTS_FILE = "data.csv";
    private static final String MAILS_FILE = "emails.csv";

    /**
     * Loads accounts from data.csv.
     */
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

    /**
     * Loads mailboxes from emails.csv.
     * Each line must have 6 columns: id, sender, receiver, title, message, date.
     */
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

    /**
     * Helper method to read emails from a BufferedReader.
     * Splits the recipients field by semicolon into a list.
     */
    private static void readFromBufferedReader(Map<String, List<Mail>> mailboxes, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                // Parse each field
                int id = Integer.parseInt(parts[0].trim());
                String sender = parts[1].trim();
                String receiverField = parts[2].trim();
                // Split the receiver field into a list (if multiple recipients are present)
                List<String> recipients = new ArrayList<>(Arrays.asList(receiverField.split(";")));
                String title = parts[3].trim();
                String message = parts[4].trim();
                Date date = new Date(Long.parseLong(parts[5].trim()));

                Mail mail = new Mail(id, title, sender, new ArrayList<>(recipients), message, date);
                // For each recipient, add the mail to its mailbox.
                // (You might want to add the same mail object for each recipient.)
                for (String recipient : recipients) {
                    mailboxes.putIfAbsent(recipient, new ArrayList<>());
                    mailboxes.get(recipient).add(mail);
                }
            } else {
                System.out.println("Skipping malformed line: " + line);
            }
        }
    }

    /**
     * Saves the mailboxes to emails.csv.
     * @param mailboxes Map of mailboxes to save.
     * @param filePath Path to the file where emails are saved.
     */
    public static void saveMailboxes(Map<String, List<Mail>> mailboxes, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) { // false to overwrite
            // To avoid duplicate entries when a mail is sent to multiple recipients,
            // you can choose to save each mail only once.
            // For simplicity, we'll iterate over all mailboxes.
            // (In a production scenario, consider storing mails separately and mapping recipients to mail IDs.)
            Set<Mail> savedMails = new HashSet<>();
            for (Map.Entry<String, List<Mail>> entry : mailboxes.entrySet()) {
                for (Mail mail : entry.getValue()) {
                    if (savedMails.contains(mail)) {
                        continue; // Already saved this mail
                    }
                    savedMails.add(mail);
                    // Join the recipients list using a semicolon.
                    String receiversJoined = String.join(";", mail.getReceiver());
                    bw.write(mail.getId() + ","
                            + mail.getSender().trim() + ","
                            + receiversJoined.trim() + ","
                            + mail.getTitle().trim() + ","
                            + mail.getMessage().trim() + ","
                            + mail.getDate().getTime());
                    bw.newLine();
                }
            }
            System.out.println("DEBUG: Saved updated mailboxes to file.");
        } catch (IOException e) {
            System.out.println("❌ Error saving emails: " + e.getMessage());
        }
    }
}