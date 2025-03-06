package com.server.model;

import com.common.Mail;

import java.io.*;
import java.util.*;

/**
 * Classe di utilità per la gestione della persistenza delle email su file CSV.
 */
public class CsvHandler {
    private static final String FILE_PATH = "/data.csv";

    /**
     * Carica le email dal file CSV e le restituisce come mappa utenti-email.
     * @return Mappa delle caselle di posta elettronica.
     */
    /*public static Map<String, List<Mail>> loadMailboxes() {
        Map<String, List<Mail>> mailboxes = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String sender = parts[0];
                String receiver = parts[1];
                String title = parts[2];
                String message = parts[3];
                Date date = new Date(Long.parseLong(parts[4]));

                Mail mail = new Mail(0, title, sender, new ArrayList<>(List.of(receiver)), message, date);
                mailboxes.putIfAbsent(receiver, new ArrayList<>());
                mailboxes.get(receiver).add(mail);
            }
        } catch (IOException e) {
            System.out.println("Errore nel caricamento delle email: " + e.getMessage());
        }
        return mailboxes;
    }*/
    public static Map<String, List<Mail>> loadMailboxes() {
        Map<String, List<Mail>> mailboxes = new HashMap<>();

        try {
            // First, try to load from an external file
            File file = new File("resources/data.csv");

            if (!file.exists()) {
                // Try to load from classpath if the external file does not exist
                InputStream in = CsvHandler.class.getResourceAsStream("/data.csv");
                if (in == null) {
                    System.out.println("Errore: file data.csv non trovato né nel percorso né nel classpath.");
                    return mailboxes;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                readFromBufferedReader(mailboxes, br);
            } else {
                // Load from external file
                BufferedReader br = new BufferedReader(new FileReader(file));
                readFromBufferedReader(mailboxes, br);
            }

        } catch (IOException e) {
            System.out.println("Errore nel caricamento delle email: " + e.getMessage());
        }
        return mailboxes;
    }

    // Helper method to read from a BufferedReader
    private static void readFromBufferedReader(Map<String, List<Mail>> mailboxes, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < 5) continue;

            String sender = parts[0];
            String receiver = parts[1];
            String title = parts[2];
            String message = parts[3];
            Date date = new Date(Long.parseLong(parts[4]));

            Mail mail = new Mail(0, title, sender, new ArrayList<>(List.of(receiver)), message, date);
            mailboxes.putIfAbsent(receiver, new ArrayList<>());
            mailboxes.get(receiver).add(mail);
        }
    }


    /**
     * Salva le email nel file CSV.
     * @param mailboxes Mappa delle caselle di posta elettronica da salvare.
     */
    public static void saveMailboxes(Map<String, List<Mail>> mailboxes) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Map.Entry<String, List<Mail>> entry : mailboxes.entrySet()) {
                for (Mail mail : entry.getValue()) {
                    bw.write(mail.getSender() + "," + String.join(";", mail.getReceiver()) + ","
                            + mail.getTitle() + "," + mail.getMessage() + "," + mail.getDate().getTime());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio delle email: " + e.getMessage());
        }
    }
}
