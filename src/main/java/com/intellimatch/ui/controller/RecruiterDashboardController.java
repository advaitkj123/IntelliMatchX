package com.intellimatch.ui.controller;

import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.UserRole;
import com.intellimatch.ui.SceneNavigator;
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
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
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
    @FXML private TextField postingTitleField;
    @FXML private TextField roleLevelField;
    @FXML private TextField locationField;
    @FXML private TextField stipendField;
    @FXML private TextField startDateField;
    @FXML private ComboBox<String> workModeComboBox;

    @FXML private TextField filterMinScoreField;
    @FXML private TextField filterBackgroundField;
    @FXML private TextField filterMinAvailabilityField;
    @FXML private TextField filterMinExactMatchesField;

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
        workModeComboBox.setItems(FXCollections.observableArrayList("Remote", "Hybrid", "Onsite"));
        workModeComboBox.getSelectionModel().select("Hybrid");
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
        List<MatchResult> filtered = applyCandidateFilters(results);
        databaseService.recordActivity(currentEmail);
        candidateTableView.setItems(FXCollections.observableArrayList(filtered));
        if (filtered.isEmpty()) {
            statusLabel.setText("No candidates matched this skill requirement.");
            matchInsightArea.setText("No candidates found.");
            return;
        }
        candidateTableView.getSelectionModel().selectFirst();
        statusLabel.setText("Found " + filtered.size() + " candidate matches after filters.");
    }

    @FXML
    private void onSavePostingControls() {
        try {
            databaseService.updateRecruiterPostingControls(
                currentEmail,
                postingTitleField.getText(),
                roleLevelField.getText(),
                locationField.getText(),
                stipendField.getText(),
                startDateField.getText(),
                workModeComboBox.getValue()
            );
            populateProfile();
            statusLabel.setText("Posting controls saved.");
        } catch (IllegalArgumentException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onShortlistSelectedCandidate() {
        MatchResult selected = candidateTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a candidate first.");
            return;
        }
        databaseService.shortlistCandidate(currentEmail, selected.getApplicant().getEmail());
        statusLabel.setText("Shortlisted " + selected.getApplicant().getName() + ".");
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
        postingTitleField.setText(recruiter.getInternshipTitle());
        roleLevelField.setText(recruiter.getRoleLevel());
        locationField.setText(recruiter.getLocation());
        stipendField.setText(recruiter.getStipend());
        startDateField.setText(recruiter.getStartDate());
        if (recruiter.getWorkMode() != null && !recruiter.getWorkMode().isBlank()) {
            workModeComboBox.getSelectionModel().select(recruiter.getWorkMode());
        }
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

    private List<MatchResult> applyCandidateFilters(List<MatchResult> results) {
        double minScore = parseDouble(filterMinScoreField.getText(), 0.0);
        int minAvailability = parseInt(filterMinAvailabilityField.getText(), 0);
        int minExactMatches = parseInt(filterMinExactMatchesField.getText(), 0);
        String backgroundKeyword = filterBackgroundField.getText() == null ? "" : filterBackgroundField.getText().trim().toLowerCase();

        return results.stream()
            .filter(result -> result.getScore() * 100 >= minScore)
            .filter(result -> result.getApplicant().getAvailabilityWeeks() >= minAvailability)
            .filter(result -> result.getMatchedSkills().size() >= minExactMatches)
            .filter(result -> backgroundKeyword.isBlank()
                || result.getApplicant().getDesiredRole().toLowerCase().contains(backgroundKeyword))
            .collect(Collectors.toList());
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Double.parseDouble(value.trim());
        } catch (RuntimeException ex) {
            return fallback;
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
