/*package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) { //lo stage ovvero la finestra e al suo interno c'è una scene con dentro dei pannelli
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
}*/

package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client-home.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Mail Client");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

//VANNO AGGIUNTE UN SACCO DI COSE (TUTTE LE OPERAZIONIIII PER POTER FARE IL LOADER DI CLIENT-OPERATION) --> o forse no ahah

