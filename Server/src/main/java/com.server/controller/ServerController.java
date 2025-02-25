package com.server.controller; // Specifica il package della classe

import com.server.model.Server; // Importa la classe Server che gestisce la logica del server
import javafx.application.Platform; // Importato per aggiornare la UI da un altro thread
import javafx.fxml.FXML; // Permette di collegare gli elementi FXML al controller
import javafx.scene.control.ListView; // Importa il componente grafico ListView
import javafx.scene.control.TextArea; // Importa il componente grafico TextArea

/**
 * Controller per il server, che gestisce la UI e l'interazione con la logica del server.
 */
public class ServerController {

    @FXML
    private TextArea logArea; // Area di testo per mostrare i log del server

    @FXML
    private ListView<String> userList; // Lista degli utenti connessi al server

    private final Server server = new Server(); // Istanza del server per gestire la logica di rete

    /**
     * Metodo chiamato automaticamente quando il controller viene inizializzato.
     * Collega i componenti grafici ai dati del server e avvia il server in un thread separato.
     */
    @FXML
    //AAAAAAAAAAAAAAAAAAAAAAA CAPISCI
    /*public void initialize() {
        // Collega la TextArea alla proprietà dei log del server
        logArea.textProperty().bind(server.getLogTableProperty());

        // Collega la ListView alla lista degli utenti connessi
        userList.itemsProperty().bind(server.getUsersProperty());

        // Mantiene il cursore sempre in fondo alla TextArea quando arrivano nuovi messaggi
        server.getCaret().addListener((obs, oldPos, newPos) ->
                logArea.positionCaret(newPos.intValue())
        );

        // Avvia il server in un nuovo thread per evitare di bloccare l'interfaccia grafica
        new Thread(server::start).start();
    }*/

    /**
     * Aggiunge un messaggio ai log del server.
     * Il metodo usa Platform.runLater() per aggiornare la UI in modo sicuro.
     *
     * @param message Il messaggio da aggiungere ai log
     */
    public void appendLog(String message) {
        // Aggiorna la TextArea nel thread principale di JavaFX
        Platform.runLater(() -> logArea.appendText(message + "\n"));

        // Stampa il messaggio anche nella console del programma
        System.out.println(message);
    }
}
