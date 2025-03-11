package com.server.model;

import com.common.Mail;
import java.io.*;
import java.util.*;

public class CsvHandler {
    // File paths:
    // - ACCOUNTS_FILE (data.csv) contains the valid accounts (one email per line)
    // - MAILS_FILE (emails.csv) contains the complete emails (6 columns: id, sender, receiver, title, message, date)
    private static final String ACCOUNTS_FILE = "data.csv";
    private static final String MAILS_FILE = "emails.csv";

    /**
     * Loads the accounts from data.csv.
     *
     * @param filePath the path to the accounts file
     * @return a Set containing all valid email addresses
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
     * Loads the mailboxes from emails.csv.
     * Each line must have 6 columns: id, sender, receiver, title, message, date.
     *
     * @param filePath the path to the emails file
     * @return a map where the key is the recipient email and the value is the list of mails received.
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
     * Helper method to read mails from a BufferedReader.
     * It expects each line to have 6 columns: id, sender, receiver, title, message, date.
     */
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
                Date date = new Date(Long.parseLong(parts[5].trim()));

                // Split the receiver field using ";" as the delimiter,
                // then create a new ArrayList so that the type is ArrayList<String>
                ArrayList<String> recipients = new ArrayList<>(Arrays.asList(receiverField.split(";")));

                // Create the Mail object with the generated id, title, sender, recipients, message, and date.
                Mail mail = new Mail(id, title, sender, recipients, message, date);

                // For each recipient, add the mail to that recipient's mailbox.
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

    /**
     * Saves the mailboxes to emails.csv.
     * Each email is saved in a separate line with 6 columns: id, sender, receiver, title, message, date.
     *
     * @param mailboxes the map of mailboxes to save.
     * @param filePath  the path to the file where emails will be saved.
     */
    public static void saveMailboxes(Map<String, List<Mail>> mailboxes, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) { // false to overwrite the file
            // Save one line per email for each recipient.
            for (Map.Entry<String, List<Mail>> entry : mailboxes.entrySet()) {
                String recipient = entry.getKey();
                for (Mail mail : entry.getValue()) {
                    // When saving, write the recipient field as the specific email address,
                    // not the entire list.
                    bw.write(mail.getId() + ","
                            + mail.getSender().trim() + ","
                            + recipient + ","
                            + mail.getTitle().trim() + ","
                            + mail.getMessage().trim() + ","
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
