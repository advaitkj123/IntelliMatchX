package com.intellimatch.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RecruiterLoginController {

    @FXML
    private void onBack(ActionEvent event) throws IOException {
        navigateTo("/fxml/landing.fxml", event);
    }

    @FXML
    private void onLogin(ActionEvent event) throws IOException {
        navigateTo("/fxml/main.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1200, 780);
        scene.getStylesheets().add(
            org.kordamp.bootstrapfx.BootstrapFX.bootstrapFXStylesheet()
        );

        stage.setScene(scene);
        stage.show();
    }
}
