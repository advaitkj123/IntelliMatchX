package com.intellimatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.net.URL;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("/fxml/landing.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find /fxml/landing.fxml on classpath");
        }

        Parent root = FXMLLoader.load(fxmlUrl);

        Scene scene = new Scene(root, 1200, 780);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        primaryStage.setTitle("IntelliMatch-X - Automated Bi-Directional Internship Matching System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(640);
        primaryStage.show();
    }
}
