module com.server {
    requires java.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires com.common;

    exports com.server;
    exports com.server.model;
    exports com.server.controller;
}
