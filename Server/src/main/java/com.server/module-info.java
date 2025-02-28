module com.server {
    requires java.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.common; // Assicura che il modulo `Common` sia richiesto

    exports com.Server;
    exports com.Server.model;
    exports com.Server.controller;
}
