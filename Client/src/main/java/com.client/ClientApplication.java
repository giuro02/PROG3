
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

