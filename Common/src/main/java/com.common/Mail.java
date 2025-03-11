package com.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta un'email con mittente, destinatari, titolo, corpo e data di invio.
 */
public class Mail implements Serializable {
    private int id;
    private String title;
    private String sender;
    private List<String> receiver; // Usare List<String> (o ArrayList) è sufficiente
    private String message;
    private Date date;

    public Mail(int id, String title, String sender, List<String> receiver, String message, Date date) {
        this.id = id;
        this.title = title;
        this.sender = sender;
        // Assicurati di salvare una nuova lista per evitare modifiche esterne
        this.receiver = new ArrayList<>(receiver);
        this.message = message;
        this.date = date;
    }

    // Getters
    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getSender() {
        return sender;
    }
    public List<String> getReceiver() {
        return new ArrayList<>(receiver);
    }
    public String getMessage() {
        return message;
    }
    public Date getDate() {
        return date;
    }

    // Modifica di equals() e hashCode() in modo da considerare l'ID e il destinatario.
    // Poiché lo stesso messaggio (con lo stesso ID) può essere inviato a più destinatari,
    // consideriamo "uguali" due email se hanno lo stesso ID e se il destinatario specifico coincide.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Mail other = (Mail) obj;
        // Supponiamo che per cancellare un'email la chiave di confronto sia l'ID e
        // che il destinatario da cancellare sia quello dell'email in memoria.
        // Qui si assume che l'email "da cancellare" abbia in receiver una lista con
        // un solo indirizzo, cioè quello della mailbox target.
        if (this.id != other.id) return false;
        if (this.receiver == null || other.receiver == null) return false;
        // Confronta solo il primo destinatario (si suppone la lista contenga solo l'indirizzo specifico)
        return this.receiver.get(0).equalsIgnoreCase(other.receiver.get(0));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, receiver != null && !receiver.isEmpty() ? receiver.get(0).toLowerCase() : "");
    }

    @Override
    public String toString() {
        return sender + ": " + title;
    }
}
