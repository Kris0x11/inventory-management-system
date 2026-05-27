package myapp.Applicazione;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Application;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        GUI gui = new GUI();

        Button stampaart = new Button("Visualizza articoli");
        Button inserisci = new Button("Inserisci articoli");

        VBox layout = new VBox(10);
        layout.getChildren().addAll(stampaart, inserisci);

        stampaart.setOnAction(e -> {
            gui.refresh(); // ← ORA FUNZIONA BENISSIMO

            // evita duplicazioni
            if (!layout.getChildren().contains(gui.getTableView())) {
                layout.getChildren().add(gui.getTableView());
            }

            if (!layout.getChildren().contains(gui.getLayout())) {
                layout.getChildren().add(gui.getLayout());
            }
        });

        inserisci.setOnAction(e -> gui.inserisci());

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setTitle("Gestione Inventario BY CHRISTIAN D'AMATO");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        DBsetup.DBsetup();
        launch(args);
    }
}
