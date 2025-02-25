package com.server.model;

public class ServerException extends Exception{
    public ServerException(String message){
        super("ServerException: " + message);
    }
}

