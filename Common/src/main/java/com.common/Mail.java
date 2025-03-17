package com.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

// Rappresenta un'email con mittente, destinatari, titolo, corpo e data di invio.
public class Mail implements Serializable {
    private int id;
    private String title;
    private String sender;
    private List<String> receiver;
    private String message;
    private Date date;

    // Costruttore: inizializza tutti i campi dell'email
    public Mail(int id, String title, String sender, List<String> receiver, String message, Date date) {
        this.id = id;
        this.title = title;
        this.sender = sender;
        this.receiver = new ArrayList<>(receiver);
        this.message = message;
        this.date = date;
    }

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

    // equals: due email sono considerate uguali se hanno lo stesso ID e lo stesso primo destinatario
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Mail other = (Mail) obj;
        if (this.id != other.id) return false;
        if (this.receiver == null || other.receiver == null) return false;
        return this.receiver.get(0).equalsIgnoreCase(other.receiver.get(0));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, receiver != null && !receiver.isEmpty() ? receiver.get(0).toLowerCase() : "");
    }

    // toString: restituisce una rappresentazione compatta (mittente e titolo)
    @Override
    public String toString() {
        return sender + ": " + title;
    }
}
