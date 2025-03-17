module com.client {
    requires java.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires com.common;

    exports com.client;
    exports com.client.model;
    exports com.client.controller;

    opens com.client to javafx.fxml;
    opens com.client.controller to javafx.fxml;
}
