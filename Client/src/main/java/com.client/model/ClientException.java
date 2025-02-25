package com.client.model;

public class ClientException extends Exception{
    public ClientException(String message){
        super("ClientException: " + message);
    }
}
