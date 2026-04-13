package com.intellimatch.ui.controller;

import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.UserRole;
import com.intellimatch.service.DatabaseService;
import com.intellimatch.service.MatchingService;
import com.intellimatch.service.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RecruiterDashboardController implements Initializable {

    @FXML private Label recruiterNameLabel;
    @FXML private Label recruiterEmailLabel;
    @FXML private Label companyLabel;
    @FXML private Label requiredSkillsLabel;
    @FXML private Label statusLabel;
    @FXML private TextField addSkillsField;

    @FXML private TableView<MatchResult> candidateTableView;
    @FXML private TableColumn<MatchResult, String> colCandidate;
    @FXML private TableColumn<MatchResult, String> colBackground;
    @FXML private TableColumn<MatchResult, String> colScore;
    @FXML private TableColumn<MatchResult, String> colMatchedSkills;
    @FXML private TextArea matchInsightArea;

    private final DatabaseService databaseService = DatabaseService.getInstance();
    private final MatchingService matchingService = new MatchingService();
    private String currentEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupSelectionListener();
        loadSession();
    }

    @FXML
    private void onAddRequiredSkills() {
        List<String> skills = parseSkills(addSkillsField.getText());
        if (skills.isEmpty()) {
            statusLabel.setText("Enter at least one skill to add.");
            return;
        }
        databaseService.addRecruiterRequiredSkills(currentEmail, skills);
        addSkillsField.clear();
        populateProfile();
        statusLabel.setText("Required skills updated.");
    }

    @FXML
    private void onFindCandidates() {
        Recruiter recruiter = databaseService.getRecruiterByEmail(currentEmail).orElse(null);
        if (recruiter == null) {
            statusLabel.setText("Recruiter profile unavailable. Please log in again.");
            return;
        }

        List<MatchResult> results = matchingService.getEngine()
            .getTopCandidatesForRecruiter(recruiter, databaseService.getAllApplicants(), 30);
        candidateTableView.setItems(FXCollections.observableArrayList(results));
        if (results.isEmpty()) {
            statusLabel.setText("No candidates matched this skill requirement.");
            matchInsightArea.setText("No candidates found.");
            return;
        }
        candidateTableView.getSelectionModel().selectFirst();
        statusLabel.setText("Found " + results.size() + " candidate matches.");
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        SessionManager.clear();
        navigateTo("/fxml/landing.fxml", event);
    }

    private void loadSession() {
        var session = SessionManager.getCurrentUser();
        if (session.isEmpty() || session.get().getRole() != UserRole.RECRUITER) {
            statusLabel.setText("Session expired. Please log in again.");
            return;
        }
        currentEmail = session.get().getEmail();
        populateProfile();
    }

    private void populateProfile() {
        Recruiter recruiter = databaseService.getRecruiterByEmail(currentEmail).orElse(null);
        if (recruiter == null) {
            statusLabel.setText("Recruiter profile unavailable.");
            return;
        }
        recruiterNameLabel.setText(recruiter.getName());
        recruiterEmailLabel.setText(recruiter.getEmail());
        companyLabel.setText(recruiter.getCompany());
        String skills = recruiter.getSkills().isEmpty()
            ? "No required skills configured"
            : recruiter.getSkills().stream().map(s -> s.getName()).collect(Collectors.joining(", "));
        requiredSkillsLabel.setText(skills);
    }

    private void setupTableColumns() {
        colCandidate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicant().getName()));
        colBackground.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicant().getDesiredRole()));
        colScore.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getScorePercentage()));
        colMatchedSkills.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getMatchedSkills().isEmpty() ? "-" : String.join(", ", data.getValue().getMatchedSkills())
        ));
    }

    private void setupSelectionListener() {
        candidateTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) return;
            matchInsightArea.setText(
                selected.getJustificationRecruiterView() + "\n\n" +
                "Applicant Perspective:\n" + selected.getJustificationApplicantView()
            );
        });
    }

    private List<String> parseSkills(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        return Arrays.stream(input.split(","))
            .map(String::trim)
            .filter(skill -> !skill.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }

    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1200, 780);
        scene.getStylesheets().add(org.kordamp.bootstrapfx.BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(scene);
        stage.show();
    }
}
