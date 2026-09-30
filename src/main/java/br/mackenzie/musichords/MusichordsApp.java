package br.mackenzie.musichords;

import br.mackenzie.musichords.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusichordsApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController mainController = new MainController();
        Scene scene = new Scene(mainController.getView(), 900, 700);
        
        // Use external stylesheet if available
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        
        primaryStage.setTitle("Musichords - Modelagem de Progressões Harmônicas");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
