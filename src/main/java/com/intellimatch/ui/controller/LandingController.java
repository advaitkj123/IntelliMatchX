package com.intellimatch.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LandingController {

    @FXML
    private void onRecruiterSelected(ActionEvent event) throws IOException {
        navigateTo("/fxml/recruiter-login.fxml", event);
    }

    @FXML
    private void onApplicantSelected(ActionEvent event) throws IOException {
        navigateTo("/fxml/applicant-login.fxml", event);
    }

    @FXML
    private void onAdminSelected(ActionEvent event) throws IOException {
        navigateTo("/fxml/admin-dashboard.fxml", event);
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
