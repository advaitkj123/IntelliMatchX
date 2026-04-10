package com.intellimatch.ui.controller;

import com.intellimatch.model.Applicant;
import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.Skill;
import com.intellimatch.service.MatchingService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * JavaFX FXML Controller for the main IntelliMatch-X window.
 * Manages the bi-directional matching UI, the notification panel,
 * and the embedded Explainability Dashboard (WebView).
 */
public class MainController implements Initializable {

    @FXML private TabPane mainTabPane;
    @FXML private ComboBox<String> strategyComboBox;
    @FXML private ComboBox<String> applicantComboBox;
    @FXML private ComboBox<String> recruiterComboBox;

    @FXML private TableView<MatchResult> matchTableView;
    @FXML private TableColumn<MatchResult, String> colApplicant;
    @FXML private TableColumn<MatchResult, String> colRecruiter;
    @FXML private TableColumn<MatchResult, String> colCompany;
    @FXML private TableColumn<MatchResult, String> colScore;
    @FXML private TableColumn<MatchResult, String> colMatchedSkills;

    @FXML private TextArea applicantJustificationArea;
    @FXML private TextArea recruiterJustificationArea;

    @FXML private ListView<String> notificationListView;

    @FXML private WebView explainabilityWebView;

    @FXML private Label statusLabel;

    private final MatchingService matchingService = new MatchingService();
    private List<MatchResult> currentResults;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStrategyComboBox();
        setupApplicantComboBox();
        setupRecruiterComboBox();
        setupTableColumns();
        setupTableSelectionListener();
        loadExplainabilityDashboard();
        statusLabel.setText("Ready. Select a strategy and run matching.");
    }

    private void setupStrategyComboBox() {
        strategyComboBox.setItems(FXCollections.observableArrayList(
            "Weighted Skill-Graph Strategy",
            "Exact Skill Count Strategy"
        ));
        strategyComboBox.getSelectionModel().selectFirst();
    }

    private void setupApplicantComboBox() {
        List<String> names = matchingService.getApplicants().stream()
            .map(a -> a.getName())
            .collect(Collectors.toList());
        applicantComboBox.setItems(FXCollections.observableArrayList(names));
        applicantComboBox.getSelectionModel().selectFirst();
    }

    private void setupRecruiterComboBox() {
        List<String> names = matchingService.getRecruiters().stream()
            .map(r -> r.getName() + " (" + r.getCompany() + ")")
            .collect(Collectors.toList());
        recruiterComboBox.setItems(FXCollections.observableArrayList(names));
        recruiterComboBox.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        colApplicant.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApplicant().getName()));
        colRecruiter.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRecruiter().getInternshipTitle()));
        colCompany.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRecruiter().getCompany()));
        colScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getScorePercentage()));
        colMatchedSkills.setCellValueFactory(d ->
            new SimpleStringProperty(String.join(", ", d.getValue().getMatchedSkills())));
    }

    private void setupTableSelectionListener() {
        matchTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    applicantJustificationArea.setText(newVal.getJustificationApplicantView());
                    recruiterJustificationArea.setText(newVal.getJustificationRecruiterView());
                    updateExplainabilityDashboard(newVal);
                }
            }
        );
    }

    @FXML
    private void onStrategyChanged() {
        String selected = strategyComboBox.getValue();
        if ("Exact Skill Count Strategy".equals(selected)) {
            matchingService.setExactMatchStrategy();
        } else {
            matchingService.setWeightedStrategy();
        }
        statusLabel.setText("Strategy switched to: " + selected);
    }

    @FXML
    private void onRunBidirectionalMatch() {
        applySelectedStrategy();
        currentResults = matchingService.runFullBidirectionalMatch();
        matchTableView.setItems(FXCollections.observableArrayList(currentResults));
        refreshNotificationFeed();
        statusLabel.setText("Bi-directional matching complete. " + currentResults.size() + " pairs evaluated.");
    }

    @FXML
    private void onGetApplicantRecommendations() {
        int idx = applicantComboBox.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        applySelectedStrategy();
        Applicant applicant = matchingService.getApplicants().get(idx);
        List<MatchResult> results = matchingService.getRecommendationsForApplicant(applicant, 10);
        matchTableView.setItems(FXCollections.observableArrayList(results));
        statusLabel.setText("Showing top opportunities for: " + applicant.getName());
    }

    @FXML
    private void onGetRecruiterCandidates() {
        int idx = recruiterComboBox.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        applySelectedStrategy();
        Recruiter recruiter = matchingService.getRecruiters().get(idx);
        List<MatchResult> results = matchingService.getTopCandidatesForRecruiter(recruiter, 10);
        matchTableView.setItems(FXCollections.observableArrayList(results));
        statusLabel.setText("Showing top candidates for: " + recruiter.getCompany());
    }

    private void applySelectedStrategy() {
        String selected = strategyComboBox.getValue();
        if ("Exact Skill Count Strategy".equals(selected)) {
            matchingService.setExactMatchStrategy();
        } else {
            matchingService.setWeightedStrategy();
        }
    }

    private void refreshNotificationFeed() {
        List<String> feed = matchingService.getNotificationLogger().getNotificationFeed();
        notificationListView.setItems(FXCollections.observableArrayList(feed));
        notificationListView.scrollTo(feed.size() - 1);
    }

    private void loadExplainabilityDashboard() {
        String html = buildExplainabilityHtml(null);
        explainabilityWebView.getEngine().loadContent(html);
    }

    private void updateExplainabilityDashboard(MatchResult result) {
        String html = buildExplainabilityHtml(result);
        explainabilityWebView.getEngine().loadContent(html);
    }

    /**
     * Generates the HTML/JS Explainability Dashboard rendered inside WebView.
     * This satisfies the multi-tech frontend requirement.
     */
    private String buildExplainabilityHtml(MatchResult result) {
        String applicantName = result != null ? result.getApplicant().getName() : "N/A";
        String recruiterCompany = result != null ? result.getRecruiter().getCompany() : "N/A";
        String role = result != null ? result.getRecruiter().getInternshipTitle() : "N/A";
        double score = result != null ? result.getScore() * 100 : 0;
        int matchedCount = result != null ? result.getMatchedSkills().size() : 0;
        int missingCount = result != null ? result.getMissingSkills().size() : 0;
        String matchedSkillsJson = result != null
            ? result.getMatchedSkills().stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", ", "[", "]"))
            : "[]";
        String missingSkillsJson = result != null
            ? result.getMissingSkills().stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", ", "[", "]"))
            : "[]";

        String recommendation = score >= 75 ? "Strong Match — Highly Recommended"
            : score >= 50 ? "Moderate Match — Worth Pursuing"
            : "Low Match — Consider Upskilling";

        String scoreColor = score >= 75 ? "#28a745" : score >= 50 ? "#ffc107" : "#dc3545";

        return
            """
      <!DOCTYPE html>
      <html lang="en">
      <head>
      <meta charset="UTF-8"/>
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <title>IntelliMatch-X Explainability Dashboard</title>
      <style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Segoe UI', Arial, sans-serif; background: #0d1117; color: #c9d1d9; padding: 20px; }
  h1 { color: #58a6ff; font-size: 1.3em; margin-bottom: 16px; letter-spacing: 0.5px; }
  .dashboard-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
  .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; }
  .card h2 { font-size: 0.85em; color: #8b949e; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 10px; }
  .score-ring-container { text-align: center; padding: 10px 0; }
  .score-value { font-size: 2.8em; font-weight: bold; color: " + scoreColor + "; }
  .score-label { font-size: 0.8em; color: #8b949e; margin-top: 4px; }
  .info-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 0.9em; }
  .info-label { color: #8b949e; }
  .info-val { color: #e6edf3; font-weight: 500; }
  .skill-list { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
  .skill-chip { padding: 3px 10px; border-radius: 20px; font-size: 0.8em; font-weight: 500; }
  .skill-match { background: #1a3a2a; color: #3fb950; border: 1px solid #3fb950; }
  .skill-missing { background: #3a1a1a; color: #f85149; border: 1px solid #f85149; }
  .recommendation-bar { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 14px 20px; display: flex; align-items: center; gap: 14px; }
  .rec-icon { font-size: 1.6em; }
  .rec-text { font-size: 0.95em; color: #e6edf3; }
  .progress-bar-bg { height: 10px; background: #21262d; border-radius: 5px; margin-top: 12px; overflow: hidden; }
  .progress-bar-fill { height: 100%; border-radius: 5px; background: " + scoreColor + "; transition: width 1s ease; }
  .header-meta { font-size: 0.8em; color: #8b949e; margin-bottom: 18px; }
  canvas { display: block; margin: 0 auto; }
</style>
</head>
<body>
<h1>IntelliMatch-X — Explainability Dashboard</h1>
<div class="header-meta">Algorithmic transparency for bi-directional internship matching</div>

<div class="dashboard-grid">
  <div class="card">
    <h2>Match Score</h2>
    <div class="score-ring-container">
      <div class="score-value">" + String.format("%.1f%%", score) + "</div>
      <div class="score-label">Overall Match Strength</div>
      <div class="progress-bar-bg">
        <div class="progress-bar-fill" id="scoreFill" style="width:0%"></div>
      </div>
    </div>
  </div>

  <div class="card">
    <h2>Pair Details</h2>
    <div class="info-row"><span class="info-label">Applicant</span><span class="info-val">" + applicantName + "</span></div>
    <div class="info-row"><span class="info-label">Company</span><span class="info-val">" + recruiterCompany + "</span></div>
    <div class="info-row"><span class="info-label">Role</span><span class="info-val">" + role + "</span></div>
    <div class="info-row"><span class="info-label">Matched Skills</span><span class="info-val" style="color:#3fb950">" + matchedCount + "</span></div>
    <div class="info-row"><span class="info-label">Skill Gaps</span><span class="info-val" style="color:#f85149">" + missingCount + "</span></div>
  </div>
</div>

<div class="card" style="margin-bottom:16px">
  <h2>Skill Breakdown</h2>
  <div style="margin-bottom:8px; font-size:0.85em; color:#8b949e">Matched Skills</div>
  <div class="skill-list" id="matchedSkills"></div>
  <div style="margin-top:12px; margin-bottom:8px; font-size:0.85em; color:#8b949e">Skill Gaps</div>
  <div class="skill-list" id="missingSkills"></div>
</div>

<div class="card" style="margin-bottom:16px">
  <h2>Score Distribution (Skill Graph)</h2>
  <canvas id="skillChart" width="560" height="140"></canvas>
</div>

<div class="recommendation-bar">
  <div class="rec-icon">" + (score >= 75 ? "✅" : score >= 50 ? "⚡" : "⚠️") + "</div>
  <div class="rec-text"><strong>Recommendation:</strong> " + recommendation + "</div>
</div>

<script>
(function() {
  var score = " + score + ";
  var matched = " + matchedSkillsJson + ";
  var missing = " + missingSkillsJson + ";

  setTimeout(function() {
    document.getElementById('scoreFill').style.width = score + '%';
  }, 100);

  var mContainer = document.getElementById('matchedSkills');
  matched.forEach(function(s) {
    var chip = document.createElement('span');
    chip.className = 'skill-chip skill-match';
    chip.textContent = s;
    mContainer.appendChild(chip);
  });
  if (matched.length === 0) {
    mContainer.innerHTML = '<span style="color:#8b949e;font-size:0.85em">No matched skills</span>';
  }

  var gContainer = document.getElementById('missingSkills');
  missing.forEach(function(s) {
    var chip = document.createElement('span');
    chip.className = 'skill-chip skill-missing';
    chip.textContent = s;
    gContainer.appendChild(chip);
  });
  if (missing.length === 0) {
    gContainer.innerHTML = '<span style="color:#3fb950;font-size:0.85em">No skill gaps detected</span>';
  }

  var canvas = document.getElementById('skillChart');
  var ctx = canvas.getContext('2d');
  var allSkills = matched.concat(missing);
  if (allSkills.length > 0) {
    var barW = Math.min(60, (canvas.width - 40) / allSkills.length - 8);
    var maxH = canvas.height - 40;
    allSkills.forEach(function(s, i) {
      var isMatch = matched.indexOf(s) >= 0;
      var barH = isMatch ? maxH * (score / 100) : maxH * 0.15;
      var x = 20 + i * (barW + 8);
      var y = canvas.height - 20 - barH;
      ctx.fillStyle = isMatch ? '#3fb950' : '#f85149';
      ctx.beginPath();
      ctx.roundRect(x, y, barW, barH, 4);
      ctx.fill();
      ctx.fillStyle = '#8b949e';
      ctx.font = '9px Arial';
      ctx.textAlign = 'center';
      var label = s.length > 8 ? s.substring(0, 7) + '…' : s;
      ctx.fillText(label, x + barW / 2, canvas.height - 4);
    });
  } else {
    ctx.fillStyle = '#8b949e';
    ctx.font = '14px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('Run matching to see skill graph', canvas.width / 2, canvas.height / 2);
  }
})();
</script>
</body>
</html>
""";
    }
}
