package com.intellimatch.ui.controller;

import com.intellimatch.model.AnalyticsSnapshot;
import com.intellimatch.ui.SceneNavigator;
import com.intellimatch.service.DatabaseService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label signedUpLabel;
    @FXML private Label activeLabel;
    @FXML private Label shortlistedLabel;
    @FXML private Label candidateCountLabel;
    @FXML private Label recruiterCountLabel;
    @FXML private Label conversionLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<MetricRow> topSkillsTable;
    @FXML private TableColumn<MetricRow, String> topRankCol;
    @FXML private TableColumn<MetricRow, String> topMetricCol;
    @FXML private TableColumn<MetricRow, String> topCountCol;

    @FXML private TableView<MetricRow> demandSkillsTable;
    @FXML private TableColumn<MetricRow, String> demandRankCol;
    @FXML private TableColumn<MetricRow, String> demandMetricCol;
    @FXML private TableColumn<MetricRow, String> demandCountCol;

    @FXML private TableView<MetricRow> workModeTable;
    @FXML private TableColumn<MetricRow, String> modeRankCol;
    @FXML private TableColumn<MetricRow, String> modeMetricCol;
    @FXML private TableColumn<MetricRow, String> modeCountCol;

    private final DatabaseService databaseService = DatabaseService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
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
        SceneNavigator.setScenePreservingWindow(stage, root);
        stage.show();
    }

    private void refreshSnapshot() {
        AnalyticsSnapshot snapshot = databaseService.getAnalyticsSnapshot();
        signedUpLabel.setText(String.valueOf(snapshot.getSignedUpUsers()));
        activeLabel.setText(String.valueOf(snapshot.getActiveUsers()));
        shortlistedLabel.setText(String.valueOf(snapshot.getShortlistedCandidates()));
        candidateCountLabel.setText(String.valueOf(snapshot.getTotalCandidates()));
        recruiterCountLabel.setText("Recruiters: " + snapshot.getTotalRecruiters());

        double activeRate = snapshot.getSignedUpUsers() == 0
            ? 0.0
            : (snapshot.getActiveUsers() * 100.0 / snapshot.getSignedUpUsers());
        double shortlistRate = snapshot.getSignedUpUsers() == 0
            ? 0.0
            : (snapshot.getShortlistedCandidates() * 100.0 / snapshot.getSignedUpUsers());
        conversionLabel.setText(String.format("Active %.1f%% -> Shortlisted %.1f%%", activeRate, shortlistRate));

        topSkillsTable.setItems(toRows(snapshot.getTopCandidateSkills()));
        demandSkillsTable.setItems(toRows(snapshot.getMarketDemandSkills()));
        workModeTable.setItems(toRows(snapshot.getWorkModeDemand()));

        statusLabel.setText("Analytics refreshed.");
    }

    private void setupTables() {
        setupTable(topRankCol, topMetricCol, topCountCol);
        setupTable(demandRankCol, demandMetricCol, demandCountCol);
        setupTable(modeRankCol, modeMetricCol, modeCountCol);
    }

    private void setupTable(
        TableColumn<MetricRow, String> rankCol,
        TableColumn<MetricRow, String> metricCol,
        TableColumn<MetricRow, String> countCol
    ) {
        rankCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().rank())));
        metricCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().metric()));
        countCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().count())));
    }

    private ObservableList<MetricRow> toRows(Map<String, Long> data) {
        if (data == null || data.isEmpty()) {
            return FXCollections.observableArrayList(new MetricRow(1, "No data available", 0L));
        }

        List<Map.Entry<String, Long>> sorted = data.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()))
            .toList();

        ObservableList<MetricRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, Long> entry = sorted.get(i);
            rows.add(new MetricRow(i + 1, entry.getKey(), entry.getValue()));
        }
        return rows;
    }

    private record MetricRow(int rank, String metric, long count) {
    }
}
