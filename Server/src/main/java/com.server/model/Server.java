package com.server.model; // Package in cui si trova la classe

import com.common.Mail; // Importa la classe Mail per gestire i messaggi
import java.io.*; // Per leggere e scrivere file
import java.util.*; // Per gestire liste e mappe

/**
 * Server che gestisce la posta elettronica degli utenti.
 * Utilizza un file CSV per memorizzare i messaggi.
 */
public class Server {
    private static Server instance; // Istanza Singleton del Server (unica nel sistema) --> per la connesione tra un solo server
    private final Map<String, List<Mail>> mailboxes; // Mappa che associa ogni utente alla sua lista di email
    private final String FILE_PATH = "resources/data.csv"; // Percorso del file CSV per salvare le email

    // Costruttore privato del Singleton per caricare le caselle di posta
    public Server() { //--> l'ho messo pubblico così lo creo in server controller
        mailboxes = new HashMap<>(); // Inizializza la mappa delle caselle di posta
        loadMailboxes(); // Carica le email dal file CSV all'avvio del server
    }

    // Metodo per ottenere l'istanza Singleton del Server
    public static Server getInstance() {
        if (instance == null) { // Se l'istanza non esiste, la crea
            instance = new Server();
        }
        return instance; // Restituisce l'istanza esistente
    }

    // Metodo per inviare un'email ai destinatari con gestione degli errori
    public synchronized List<String> sendMail(Mail mail) {
        if (!mailboxes.containsKey(mail.getSender())) {
            return List.of("ERRORE: Il mittente non esiste"); //  Errore: mittente inesistente
        }

        List<String> invalidRecipients = new ArrayList<>(); //  Lista destinatari non validi

        // Per ogni destinatario, verifica se esiste prima di inviare l'email
        for (String recipient : mail.getReceiver()) {
            if (!mailboxes.containsKey(recipient)) { //  Controlla se il destinatario esiste
                invalidRecipients.add(recipient); //  Aggiunge alla lista degli indirizzi errati
            } else {
                mailboxes.get(recipient).add(mail); //  Se esiste, aggiunge l'email alla casella
            }
        }

        saveMailboxes(); // Salva le caselle di posta nel file

        if (!invalidRecipients.isEmpty()) {
            return invalidRecipients; //  Restituisce la lista dei destinatari inesistenti
        }

        return List.of("SUCCESSO"); //  Se tutti i destinatari erano validi, restituisce SUCCESSO
    }


    // Metodo per caricare le email salvate dal file CSV
    private void loadMailboxes() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) { // Legge ogni riga del file
                String[] parts = line.split(","); // Divide la riga in campi separati da virgole
                String sender = parts[0]; // Mittente dell'email
                String receiver = parts[1]; // Destinatario dell'email
                String title = parts[2]; // Titolo dell'email
                String message = parts[3]; // Testo dell'email
                Date date = new Date(Long.parseLong(parts[4])); // Converte la data in formato `Date`

                // Crea un oggetto Mail e lo aggiunge alla casella di posta del destinatario
                Mail mail = new Mail(0, title, sender, new ArrayList<>(List.of(receiver)), message, date);
                if (!mailboxes.containsKey(receiver)) {
                    mailboxes.put(receiver, new ArrayList<>()); // Se il destinatario non ha ancora una lista, la crea
                }
                mailboxes.get(receiver).add(mail);
            }
        } catch (IOException e) {
            System.out.println("Errore nel caricamento delle email: " + e.getMessage());
        }
    }

    // Metodo per salvare le email nel file CSV
    private void saveMailboxes() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Map.Entry<String, List<Mail>> entry : mailboxes.entrySet()) {
                for (Mail mail : entry.getValue()) {
                    bw.write(mail.getSender() + "," + String.join(";", mail.getReceiver()) + ","
                            + mail.getTitle() + "," + mail.getMessage() + "," + mail.getDate().getTime());
                    bw.newLine(); // Scrive una nuova riga nel file
                }
            }
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio delle email: " + e.getMessage());
        }
    }
}
