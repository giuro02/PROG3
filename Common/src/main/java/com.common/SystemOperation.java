package com.common;

import java.io.Serializable;

/**
 * Classe per rappresentare un'operazione tra client e server.
 */
public class SystemOperation implements Serializable {
    private final String name;
    private final int operation;
    private final Object body;

    public SystemOperation(String name, int operation) {
        this.name = name;
        this.operation = operation;
        this.body = null;
    }

    public SystemOperation(String name, int operation, Object body) {
        this.name = name;
        this.operation = operation;
        this.body = body;
    }

    public String getName() { return name; }
    public int getOperation() { return operation; }
    public Object getBody() { return body; }
}
