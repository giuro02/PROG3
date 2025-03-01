package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/client/view/ClientView.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setTitle("Mail Client");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento dell'interfaccia grafica.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
