module com.server {
    requires java.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires com.common;

    exports com.server;
    exports com.server.model;
    exports com.server.controller;

    opens com.server to javafx.fxml;
    opens com.server.controller to javafx.fxml;
}