package com.intellimatch.ui.controller;

import com.intellimatch.model.UserAccount;
import com.intellimatch.service.AuthService;
import com.intellimatch.service.DatabaseService;
import com.intellimatch.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecruiterLoginController {

    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;

    @FXML private TextField signUpRecruiterNameField;
    @FXML private TextField signUpCompanyField;
    @FXML private TextField signUpSkillsField;
    @FXML private TextField signUpEmailField;
    @FXML private PasswordField signUpPasswordField;
    @FXML private Label statusLabel;

    private final AuthService authService = new AuthService();
    private final DatabaseService databaseService = DatabaseService.getInstance();

    @FXML
    private void initialize() {
        statusLabel.setText("Seed login example: talent1@google.com / " + databaseService.getDefaultSeedPassword());
    }

    @FXML
    private void onBack(ActionEvent event) throws IOException {
        SessionManager.clear();
        navigateTo("/fxml/landing.fxml", event);
    }

    @FXML
    private void onLogin(ActionEvent event) throws IOException {
        Optional<UserAccount> account = authService.loginRecruiter(
            loginEmailField.getText(),
            loginPasswordField.getText()
        );
        if (account.isEmpty()) {
            statusLabel.setText("Invalid credentials or unauthorized role for recruiter portal.");
            return;
        }

        SessionManager.setCurrentUser(account.get());
        navigateTo("/fxml/recruiter-dashboard.fxml", event);
    }

    @FXML
    private void onSignUp(ActionEvent event) throws IOException {
        List<String> skills = parseSkills(signUpSkillsField.getText());
        try {
            UserAccount account = authService.signUpRecruiter(
                signUpRecruiterNameField.getText(),
                signUpCompanyField.getText(),
                signUpEmailField.getText(),
                signUpPasswordField.getText(),
                skills
            );
            SessionManager.setCurrentUser(account);
            navigateTo("/fxml/recruiter-dashboard.fxml", event);
        } catch (IllegalArgumentException ex) {
            statusLabel.setText(ex.getMessage());
        }
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

    private List<String> parseSkills(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(skill -> !skill.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }
}
