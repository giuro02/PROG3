module com.client {
    requires java.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.common; // Importa il modulo per usare `Mail`

    exports com.client;
    exports com.client.model;
    exports com.client.controller;
}
