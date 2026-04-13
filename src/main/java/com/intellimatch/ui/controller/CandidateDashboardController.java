package com.intellimatch.ui.controller;

import com.intellimatch.model.Applicant;
import com.intellimatch.model.CompanySkillMatchResult;
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

public class CandidateDashboardController implements Initializable {

    @FXML private Label candidateNameLabel;
    @FXML private Label candidateEmailLabel;
    @FXML private Label currentSkillsLabel;
    @FXML private Label statusLabel;
    @FXML private TextField addSkillsField;

    @FXML private TableView<CompanySkillMatchResult> companyTableView;
    @FXML private TableColumn<CompanySkillMatchResult, String> colCompany;
    @FXML private TableColumn<CompanySkillMatchResult, String> colRole;
    @FXML private TableColumn<CompanySkillMatchResult, String> colScore;
    @FXML private TableColumn<CompanySkillMatchResult, String> colExact;
    @FXML private TableColumn<CompanySkillMatchResult, String> colSimilar;
    @FXML private TextArea matchInsightArea;

    private final DatabaseService databaseService = DatabaseService.getInstance();
    private final MatchingService matchingService = new MatchingService();
    private String currentEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupTableSelection();
        loadSession();
    }

    @FXML
    private void onAddSkills() {
        List<String> newSkills = parseSkills(addSkillsField.getText());
        if (newSkills.isEmpty()) {
            statusLabel.setText("Add at least one skill to continue.");
            return;
        }
        databaseService.addCandidateSkills(currentEmail, newSkills);
        addSkillsField.clear();
        populateProfile();
        statusLabel.setText("Skills added to your profile.");
    }

    @FXML
    private void onFindCompanyMatches() {
        Applicant applicant = databaseService.getApplicantByEmail(currentEmail).orElse(null);
        if (applicant == null) {
            statusLabel.setText("Candidate profile unavailable. Please log in again.");
            return;
        }

        List<String> skills = applicant.getSkills().stream().map(s -> s.getName()).collect(Collectors.toList());
        if (skills.isEmpty()) {
            statusLabel.setText("Please add skills before searching for company matches.");
            return;
        }

        List<CompanySkillMatchResult> matches =
            matchingService.findCompaniesByCandidateSkills(applicant.getName(), skills, 30);
        databaseService.recordActivity(currentEmail);
        companyTableView.setItems(FXCollections.observableArrayList(matches));
        if (matches.isEmpty()) {
            matchInsightArea.setText("No company matches found for the current skill set.");
            statusLabel.setText("No matches found. Try adding more skills.");
            return;
        }

        companyTableView.getSelectionModel().selectFirst();
        statusLabel.setText("Found " + matches.size() + " matching company opportunities.");
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        SessionManager.clear();
        navigateTo("/fxml/landing.fxml", event);
    }

    private void loadSession() {
        var session = SessionManager.getCurrentUser();
        if (session.isEmpty() || session.get().getRole() != UserRole.CANDIDATE) {
            statusLabel.setText("Session expired. Please log in again.");
            return;
        }
        currentEmail = session.get().getEmail();
        populateProfile();
    }

    private void populateProfile() {
        Applicant applicant = databaseService.getApplicantByEmail(currentEmail).orElse(null);
        if (applicant == null) {
            statusLabel.setText("Candidate profile unavailable.");
            return;
        }
        candidateNameLabel.setText(applicant.getName());
        candidateEmailLabel.setText(applicant.getEmail());
        String skills = applicant.getSkills().isEmpty()
            ? "No skills added yet"
            : applicant.getSkills().stream().map(s -> s.getName()).collect(Collectors.joining(", "));
        currentSkillsLabel.setText(skills);
    }

    private void setupTableColumns() {
        colCompany.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompany()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInternshipTitle()));
        colScore.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getScorePercentage()));
        colExact.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getExactMatchedSkills().isEmpty() ? "-" : String.join(", ", data.getValue().getExactMatchedSkills())
        ));
        colSimilar.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getSimilarMatchedSkills().isEmpty() ? "-" : String.join(", ", data.getValue().getSimilarMatchedSkills())
        ));
    }

    private void setupTableSelection() {
        companyTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) return;
            String exact = selected.getExactMatchedSkills().isEmpty() ? "None" : String.join(", ", selected.getExactMatchedSkills());
            String similar = selected.getSimilarMatchedSkills().isEmpty() ? "None" : String.join(", ", selected.getSimilarMatchedSkills());
            String missing = selected.getMissingSkills().isEmpty() ? "None" : String.join(", ", selected.getMissingSkills());

            matchInsightArea.setText(
                "Company: " + selected.getCompany() + "\n" +
                "Role: " + selected.getInternshipTitle() + "\n" +
                "Score: " + selected.getScorePercentage() + "\n\n" +
                "Exact Skills: " + exact + "\n" +
                "Similar Skills: " + similar + "\n" +
                "Missing Skills: " + missing
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
        SceneNavigator.setScenePreservingWindow(stage, root);
        stage.show();
    }
}
