package com.intellimatch.ui.controller;

import com.intellimatch.model.AnalyticsSnapshot;
import com.intellimatch.service.DatabaseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML private Label signedUpLabel;
    @FXML private Label activeLabel;
    @FXML private Label shortlistedLabel;
    @FXML private Label candidateCountLabel;
    @FXML private Label recruiterCountLabel;
    @FXML private Label conversionLabel;
    @FXML private Label statusLabel;

    @FXML private TextArea topSkillsArea;
    @FXML private TextArea demandSkillsArea;
    @FXML private TextArea workModeDemandArea;

    private final DatabaseService databaseService = DatabaseService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshSnapshot();
    }

    @FXML
    private void onRefresh() {
        refreshSnapshot();
    }

    @FXML
    private void onBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1200, 780);
        scene.getStylesheets().add(org.kordamp.bootstrapfx.BootstrapFX.bootstrapFXStylesheet());
        stage.setScene(scene);
        stage.show();
    }

    private void refreshSnapshot() {
        AnalyticsSnapshot snapshot = databaseService.getAnalyticsSnapshot();
        signedUpLabel.setText(String.valueOf(snapshot.getSignedUpUsers()));
        activeLabel.setText(String.valueOf(snapshot.getActiveUsers()));
        shortlistedLabel.setText(String.valueOf(snapshot.getShortlistedCandidates()));
        candidateCountLabel.setText(String.valueOf(snapshot.getTotalCandidates()));
        recruiterCountLabel.setText(String.valueOf(snapshot.getTotalRecruiters()));

        double activeRate = snapshot.getSignedUpUsers() == 0
            ? 0.0
            : (snapshot.getActiveUsers() * 100.0 / snapshot.getSignedUpUsers());
        double shortlistRate = snapshot.getSignedUpUsers() == 0
            ? 0.0
            : (snapshot.getShortlistedCandidates() * 100.0 / snapshot.getSignedUpUsers());
        conversionLabel.setText(String.format("Active %.1f%% -> Shortlisted %.1f%%", activeRate, shortlistRate));

        topSkillsArea.setText(formatRankedMap(snapshot.getTopCandidateSkills()));
        demandSkillsArea.setText(formatRankedMap(snapshot.getMarketDemandSkills()));
        workModeDemandArea.setText(formatRankedMap(snapshot.getWorkModeDemand()));
        statusLabel.setText("Analytics refreshed.");
    }

    private String formatRankedMap(Map<String, Long> data) {
        if (data == null || data.isEmpty()) {
            return "No data available.";
        }
        return data.entrySet().stream()
            .map(entry -> entry.getKey() + " : " + entry.getValue())
            .collect(Collectors.joining("\n"));
    }
}
