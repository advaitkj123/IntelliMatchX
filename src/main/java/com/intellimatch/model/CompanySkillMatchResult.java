package com.intellimatch.model;

import java.util.List;

/**
 * Backend response model for candidate-input skill search across companies.
 * Separates exact and similar skill matches for explainability.
 */
public class CompanySkillMatchResult {

    private final String candidateName;
    private final String company;
    private final String recruiterName;
    private final String internshipTitle;
    private final double score;
    private final List<String> exactMatchedSkills;
    private final List<String> similarMatchedSkills;
    private final List<String> missingSkills;

    public CompanySkillMatchResult(
            String candidateName,
            String company,
            String recruiterName,
            String internshipTitle,
            double score,
            List<String> exactMatchedSkills,
            List<String> similarMatchedSkills,
            List<String> missingSkills) {
        this.candidateName = candidateName;
        this.company = company;
        this.recruiterName = recruiterName;
        this.internshipTitle = internshipTitle;
        this.score = score;
        this.exactMatchedSkills = List.copyOf(exactMatchedSkills);
        this.similarMatchedSkills = List.copyOf(similarMatchedSkills);
        this.missingSkills = List.copyOf(missingSkills);
    }

    public String getCandidateName() { return candidateName; }
    public String getCompany() { return company; }
    public String getRecruiterName() { return recruiterName; }
    public String getInternshipTitle() { return internshipTitle; }
    public double getScore() { return score; }
    public List<String> getExactMatchedSkills() { return exactMatchedSkills; }
    public List<String> getSimilarMatchedSkills() { return similarMatchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }

    public String getScorePercentage() {
        return String.format("%.1f%%", score * 100);
    }
}
