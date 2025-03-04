package com.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Rappresenta un'email con mittente, destinatari, titolo, corpo e data di invio.
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

    // Getters
    public String getSender() { return sender; }
    public ArrayList<String> getReceiver() { return receiver; }
    public String getMessage() { return message; }
    public Date getDate() { return date; }
    public String getTitle() { return title; }

    @Override
    public String toString() {
        return sender + ": " + title;
    }


}
