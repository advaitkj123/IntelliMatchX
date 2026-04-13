package com.intellimatch.ui.controller;

import com.intellimatch.ui.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class LandingController {

    private static final String ADMIN_UNLOCK_SEQUENCE = "RARA";
    private final StringBuilder unlockProgress = new StringBuilder();

    @FXML private Button adminButton;
    @FXML private Label unlockHintLabel;

    @FXML
    private void initialize() {
        adminButton.setManaged(false);
        adminButton.setVisible(false);
        unlockHintLabel.setText("Admin portal locked.");
    }

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

    @FXML
    private void onRecruiterSecretTap(MouseEvent event) {
        registerUnlockTap('R', event);
    }

    @FXML
    private void onApplicantSecretTap(MouseEvent event) {
        registerUnlockTap('A', event);
    }

    private void registerUnlockTap(char symbol, MouseEvent event) {
        if (event.getButton() != MouseButton.SECONDARY || adminButton.isVisible()) {
            return;
        }

        unlockProgress.append(symbol);
        if (unlockProgress.length() > ADMIN_UNLOCK_SEQUENCE.length()) {
            unlockProgress.delete(0, unlockProgress.length() - ADMIN_UNLOCK_SEQUENCE.length());
        }

        if (ADMIN_UNLOCK_SEQUENCE.contentEquals(unlockProgress)) {
            adminButton.setManaged(true);
            adminButton.setVisible(true);
            unlockHintLabel.setText("Admin portal unlocked.");
        } else {
            unlockHintLabel.setText("Admin portal locked.");
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        SceneNavigator.setScenePreservingWindow(stage, root);
        stage.show();
    }
}
