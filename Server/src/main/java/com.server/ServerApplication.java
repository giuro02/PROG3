/*package com.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Applicazione JavaFX per il server.

public class ServerApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("/server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Server Mail - Log");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}*/
package com.server;

import com.server.model.ServerHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the server view (FXML)
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("/server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Server Mail - Log");
        stage.setScene(scene);
        stage.show();

        // Start the server handler on a separate thread
        new Thread(() -> ServerHandler.startServer()).start();
    }

    public static void main(String[] args) {
        launch();
    }
}

