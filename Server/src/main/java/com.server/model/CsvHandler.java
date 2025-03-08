/*package com.server.model;

import com.common.Mail;

import java.io.*;
import java.util.*;

/**
 * Classe di utilità per la gestione della persistenza delle email su file CSV.

public class CsvHandler {
    private static final String FILE_PATH = "emails.csv";


    /**
     * Carica le email dal file CSV e le restituisce come mappa utenti-email.
     * @return Mappa delle caselle di posta elettronica.


    public static Map<String, List<Mail>> loadMailboxes() {
        Map<String, List<Mail>> mailboxes = new HashMap<>();

        try {
            // Debug print to check working directory
            System.out.println("Current working directory: " + new File(".").getAbsolutePath());
            File file = new File(FILE_PATH);

            if (!file.exists()) {
                System.out.println("Errore: file data.csv non trovato in " + file.getAbsolutePath());
                return mailboxes;
            } else {
                BufferedReader br = new BufferedReader(new FileReader(file));
                readFromBufferedReader(mailboxes, br);
            }
        } catch (IOException e) {
            System.out.println("Errore nel caricamento delle email: " + e.getMessage());
        }
        return mailboxes;
    }

    // Helper method remains the same
    private static void readFromBufferedReader(Map<String, List<Mail>> mailboxes, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 1) {
                // La riga contiene solo l'indirizzo email: crea una casella vuota
                String userEmail = parts[0].trim();
                mailboxes.putIfAbsent(userEmail, new ArrayList<>());
            } else if (parts.length == 5) {
                // La riga contiene tutti i dati di una email
                String sender = parts[0].trim();
                String receiver = parts[1].trim();
                String title = parts[2].trim();
                String message = parts[3].trim();
                Date date = new Date(Long.parseLong(parts[4].trim()));

                Mail mail = new Mail(0, title, sender, new ArrayList<>(List.of(receiver)), message, date);
                mailboxes.putIfAbsent(receiver, new ArrayList<>());
                mailboxes.get(receiver).add(mail);
            } else {
                System.out.println("Skipping malformed line: " + line);
            }
        }
    }


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
     * Salva le email nel file CSV.
     * @param mailboxes Mappa delle caselle di posta elettronica da salvare.

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
*/
package com.server.model;

import com.common.Mail;
import java.io.*;
import java.util.*;

public class CsvHandler {
    // Percorsi dei file:
    // - data.csv conterrà gli account (un indirizzo per riga)
    // - emails.csv conterrà le email complete (5 colonne: mittente, destinatario, oggetto, testo, data)
    private static final String ACCOUNTS_FILE = "data.csv";
    private static final String MAILS_FILE = "emails.csv";

    /**
     * Carica gli account dal file data.csv.
     * Ogni riga deve contenere un solo indirizzo email.
     *
     * @param filePath percorso del file da cui leggere gli account
     * @return un Set contenente tutti gli indirizzi email validi
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
     * Ogni riga deve avere 5 colonne: mittente,destinatario,oggetto,testo,data
     *
     * @param filePath percorso del file da cui leggere le email
     * @return una mappa in cui la chiave è l'indirizzo email del destinatario e il valore la lista di email ricevute
     */
    public static Map<String, List<Mail>> loadMailboxes(String filePath) {
        Map<String, List<Mail>> mailboxes = new HashMap<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File " + filePath + " non trovato. Nessuna email caricata.");
            return mailboxes;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            readMailsFromBufferedReader(mailboxes, br);
        } catch (IOException e) {
            System.out.println("Errore nel caricamento delle email: " + e.getMessage());
        }
        return mailboxes;
    }

    // Metodo di supporto per leggere le email da un BufferedReader.
    private static void readMailsFromBufferedReader(Map<String, List<Mail>> mailboxes, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 5) {
                String sender = parts[0].trim();
                String receiver = parts[1].trim();
                String title = parts[2].trim();
                String message = parts[3].trim();
                Date date = new Date(Long.parseLong(parts[4].trim()));

                Mail mail = new Mail(0, title, sender, new ArrayList<>(List.of(receiver)), message, date);
                mailboxes.putIfAbsent(receiver, new ArrayList<>());
                mailboxes.get(receiver).add(mail);
            } else {
                System.out.println("Riga malformata, ignorata: " + line);
            }
        }
    }

    /**
     * Salva le mailbox (le email) nel file emails.csv.
     *
     * @param mailboxes Mappa delle caselle di posta da salvare.
     * @param filePath  percorso del file in cui salvare le email
     */
    public static void saveMailboxes(Map<String, List<Mail>> mailboxes, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, List<Mail>> entry : mailboxes.entrySet()) {
                for (Mail mail : entry.getValue()) {
                    bw.write(mail.getSender() + ","
                            + String.join(";", mail.getReceiver()) + ","
                            + mail.getTitle() + ","
                            + mail.getMessage() + ","
                            + mail.getDate().getTime());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio delle email: " + e.getMessage());
        }
    }
}
