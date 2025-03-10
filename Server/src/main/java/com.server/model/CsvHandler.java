package com.server.model;

import com.common.Mail;
import java.io.*;
import java.util.*;

public class CsvHandler {
    private static final String ACCOUNTS_FILE = "data.csv";
    private static final String MAILS_FILE = "emails.csv";

    /**
     * Carica gli account dal file data.csv.
     */
    public static Set<String> loadAccounts(String filePath) {
        Set<String> accounts = new HashSet<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File " + filePath + " non trovato. Nessun account caricato.");
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
     * Carica le mailbox dal file emails.csv.
     */
    public static Map<String, List<Mail>> loadMailboxes(String filePath) {
        Map<String, List<Mail>> mailboxes = new HashMap<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File " + filePath + " non trovato. Nessuna email caricata.");
            return mailboxes;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            readFromBufferedReader(mailboxes, br);
        } catch (IOException e) {
            System.out.println("Errore nel caricamento delle email: " + e.getMessage());
        }
        return mailboxes;
    }

    /**
     * Metodo separato per leggere dal BufferedReader.
     */
    private static void readFromBufferedReader(Map<String, List<Mail>> mailboxes, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                int id = Integer.parseInt(parts[0].trim());  // Convert to int ✅
                String sender = parts[1].trim();
                String receiver = parts[2].trim();
                String title = parts[3].trim();
                String message = parts[4].trim();
                Date date = new Date(Long.parseLong(parts[5].trim()));

                Mail mail = new Mail(id, title, sender, new ArrayList<>(List.of(receiver)), message, date);
                mailboxes.putIfAbsent(receiver, new ArrayList<>());
                mailboxes.get(receiver).add(mail);
            } else {
                System.out.println("Riga malformata, ignorata: " + line);
            }
        }
    }

    /**
     * Salva le mailbox nel file emails.csv.
     */
    public static void saveMailboxes(Map<String, List<Mail>> mailboxes, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (Map.Entry<String, List<Mail>> entry : mailboxes.entrySet()) {
                for (Mail mail : entry.getValue()) {
                    bw.write(mail.getId() + ","  // Now using int ✅
                            + mail.getSender().trim() + ","
                            + String.join(";", mail.getReceiver()).trim() + ","
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
