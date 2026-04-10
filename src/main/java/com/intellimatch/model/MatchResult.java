package com.intellimatch.model;

import java.util.List;

/**
 * Encapsulates the result of a bi-directional match between an Applicant and a Recruiter.
 * Supports symmetrical explainability for both sides.
 */
public class MatchResult {

    private final Applicant applicant;
    private final Recruiter recruiter;
    private final double score;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;
    private final String justificationApplicantView;
    private final String justificationRecruiterView;

    public MatchResult(
            Applicant applicant,
            Recruiter recruiter,
            double score,
            List<String> matchedSkills,
            List<String> missingSkills,
            String justificationApplicantView,
            String justificationRecruiterView) {
        this.applicant = applicant;
        this.recruiter = recruiter;
        this.score = score;
        this.matchedSkills = List.copyOf(matchedSkills);
        this.missingSkills = List.copyOf(missingSkills);
        this.justificationApplicantView = justificationApplicantView;
        this.justificationRecruiterView = justificationRecruiterView;
    }

    public Applicant getApplicant() { return applicant; }
    public Recruiter getRecruiter() { return recruiter; }
    public double getScore() { return score; }
    public List<String> getMatchedSkills() { return matchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
    public String getJustificationApplicantView() { return justificationApplicantView; }
    public String getJustificationRecruiterView() { return justificationRecruiterView; }

    public String getScorePercentage() {
        return String.format("%.1f%%", score * 100);
    }

    @Override
    public String toString() {
        return String.format(
            "MatchResult[%s <-> %s @ %s | matched=%s | missing=%s]",
            applicant.getName(), recruiter.getCompany(), getScorePercentage(),
            matchedSkills, missingSkills
        );
    }
}
