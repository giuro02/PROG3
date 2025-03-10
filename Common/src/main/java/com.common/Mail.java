package com.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.*;

/**
 * Rappresenta un'email con ID, mittente, destinatari, titolo, corpo e data di invio.
 */
public class Mail implements Serializable {
    private int id;
    private String title;
    private String sender;
    private ArrayList<String> receiver;
    private String message;
    private Date date;

    public Mail(int id, String title, String sender, ArrayList<String> receiver, String message, Date date) {
        this.id = id;
        this.title = title;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.date = date;
    }

    // Metodo getter per l'ID (necessario per CsvHandler)
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSender() {
        return sender;
    }

    public ArrayList<String> getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Mail mail = (Mail) obj;
        return id == mail.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + ": " + sender + " - " + title;
    }
}
