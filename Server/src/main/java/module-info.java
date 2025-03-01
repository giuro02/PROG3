module com.server {
    requires java.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.common; // Assicura che il modulo `Common` sia richiesto

    exports com.server;
    exports com.server.model;
    exports com.server.controller;
}
