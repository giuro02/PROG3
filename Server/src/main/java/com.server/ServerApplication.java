package com.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.server.model.ServerHandler;

public class ServerApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("/server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Server Mail - Log");
        stage.setScene(scene);
        stage.show();

        // Start the server handler on a separate thread
        Thread serverThread = new Thread(ServerHandler::startServer);
        serverThread.setDaemon(true); // Daemon thread will exit when the application closes
        serverThread.start();

        // Shutdown hook to stop the server properly when application exits
        stage.setOnCloseRequest(event -> {
            ServerHandler.stopServer(); // Stop the server when closing
            Platform.exit();  // Close the JavaFX application
            System.exit(0);  // Ensure all threads are terminated
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
