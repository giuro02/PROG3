package com.client.model;

import com.common.Mail;
import java.io.*;
import java.net.Socket;
import java.util.Map;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4000;

    // Singleton per avere un'unica istanza condivisa
    private static Client instance;

    private Client() { }

    // Restituisce l'istanza unica di Client
    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    // Effettua il login inviando il comando "LOGIN" e l'email al server
    public String login(String email) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("LOGIN");
            out.writeObject(email);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Recupera l'inbox dell'utente inviando il comando "GET_INBOX" al server
    public Map<String, Object> getInbox(String userEmail) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_INBOX");
            out.writeObject(userEmail);
            out.flush();
            return (Map<String, Object>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Invia una mail al server usando il comando "SEND_MAIL"
    public String sendMail(Mail mail) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("SEND_MAIL");
            out.writeObject(mail);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Richiede al server di cancellare una mail, inviando il comando "DELETE_MAIL"
    public String deleteMail(String userEmail, Mail mail) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("DELETE_MAIL");
            out.writeObject(userEmail);
            out.writeObject(mail);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Richiede al server di marcare una mail come letta, inviando il comando "MARK_READ"
    public String markMailAsRead(String userEmail, int mailId) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("MARK_READ");
            out.writeObject(userEmail);
            out.writeObject(mailId);
            out.flush();
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
