package com.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.server.model.ServerHandler;

public class ServerApplication extends Application {

    // Avvia la UI del server e il ServerHandler su un thread separato
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("/server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Server Mail - Log");
        stage.setScene(scene);
        stage.show();

        // Avvia il server handler su un thread separato
        Thread serverThread = new Thread(ServerHandler::startServer);
        serverThread.setDaemon(true); // Il thread daemon esce quando l'applicazione chiude
        serverThread.start();

        // Gestisce la chiusura dell'applicazione, fermando il server
        stage.setOnCloseRequest(event -> {
            ServerHandler.stopServer();
            Platform.exit();  // Chiude l'applicazione JavaFX
            System.exit(0);  // Assicura la terminazione di tutti i thread
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
