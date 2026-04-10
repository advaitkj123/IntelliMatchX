package com.intellimatch.strategy;

import com.intellimatch.model.Applicant;
import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern - Concrete: Weighted Skill-Graph Matching Algorithm.
 *
 * Algorithm:
 *   1. Build a skill-weight map for the recruiter's required skills.
 *   2. Iterate over the applicant's skills; for each match, accumulate weighted score.
 *   3. Normalise by the total possible weight (sum of all recruiter skill weights).
 *   4. Score = sum(matched weights) / sum(all required weights).
 *   5. A bonus availability factor adjusts the raw score if the applicant's availability
 *      covers at least 80% of the internship duration.
 *
 * Symmetrical explainability is generated for both the applicant's and recruiter's perspectives.
 */
public class WeightedSkillGraphStrategy implements MatchingStrategy {

    private static final double AVAILABILITY_BONUS = 0.05;
    private static final double AVAILABILITY_THRESHOLD = 0.80;

    @Override
    public MatchResult match(Applicant applicant, Recruiter recruiter) {
        Map<String, Double> requiredSkillMap = buildSkillMap(recruiter.getSkills());
        double totalRequiredWeight = requiredSkillMap.values().stream().mapToDouble(Double::doubleValue).sum();

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        double accumulatedWeight = 0.0;

        for (Map.Entry<String, Double> entry : requiredSkillMap.entrySet()) {
            String skillName = entry.getKey();
            double skillWeight = entry.getValue();

            boolean hasSkill = applicant.getSkills().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(skillName));

            if (hasSkill) {
                matchedSkills.add(skillName);
                accumulatedWeight += skillWeight;
            } else {
                missingSkills.add(skillName);
            }
        }

        double rawScore = (totalRequiredWeight > 0) ? (accumulatedWeight / totalRequiredWeight) : 0.0;

        double availabilityRatio = (recruiter.getDurationWeeks() > 0)
            ? (double) applicant.getAvailabilityWeeks() / recruiter.getDurationWeeks()
            : 1.0;

        double finalScore = rawScore;
        if (availabilityRatio >= AVAILABILITY_THRESHOLD) {
            finalScore = Math.min(1.0, rawScore + AVAILABILITY_BONUS);
        }

        String applicantView = buildApplicantJustification(applicant, recruiter, matchedSkills, missingSkills, finalScore, availabilityRatio);
        String recruiterView = buildRecruiterJustification(applicant, recruiter, matchedSkills, missingSkills, finalScore, availabilityRatio);

        return new MatchResult(applicant, recruiter, finalScore, matchedSkills, missingSkills, applicantView, recruiterView);
    }

    private Map<String, Double> buildSkillMap(List<Skill> skills) {
        Map<String, Double> map = new HashMap<>();
        for (Skill s : skills) {
            map.put(s.getName(), s.getWeight());
        }
        return map;
    }

    private String buildApplicantJustification(
            Applicant applicant, Recruiter recruiter,
            List<String> matched, List<String> missing,
            double score, double availabilityRatio) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("== Match Analysis for %s ==\n", applicant.getName()));
        sb.append(String.format("Opportunity: %s at %s\n\n", recruiter.getInternshipTitle(), recruiter.getCompany()));
        sb.append(String.format("Overall Match Score: %.1f%%\n\n", score * 100));
        sb.append("WHY THIS IS A MATCH (Your Perspective):\n");
        sb.append(String.format("  Your skills align with %d of %d required competencies.\n",
            matched.size(), matched.size() + missing.size()));

        if (!matched.isEmpty()) {
            sb.append("  Matched Skills: ").append(String.join(", ", matched)).append("\n");
        }
        if (!missing.isEmpty()) {
            sb.append("  Skills to develop: ").append(String.join(", ", missing)).append("\n");
        }

        sb.append(String.format("\n  Availability Coverage: %.0f%% of the %d-week internship duration.\n",
            Math.min(availabilityRatio * 100, 100), recruiter.getDurationWeeks()));

        if (availabilityRatio < AVAILABILITY_THRESHOLD) {
            sb.append("  Note: Your availability window may not fully cover the internship duration.\n");
        }

        sb.append("\nRECOMMENDATION: ");
        if (score >= 0.75) {
            sb.append("Strong match. Applying is highly recommended.");
        } else if (score >= 0.50) {
            sb.append("Moderate match. Consider applying and highlighting transferable skills.");
        } else {
            sb.append("Partial match. Focus on upskilling the missing areas before applying.");
        }
        return sb.toString();
    }

    private String buildRecruiterJustification(
            Applicant applicant, Recruiter recruiter,
            List<String> matched, List<String> missing,
            double score, double availabilityRatio) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("== Candidate Analysis: %s ==\n", applicant.getName()));
        sb.append(String.format("Position: %s at %s\n\n", recruiter.getInternshipTitle(), recruiter.getCompany()));
        sb.append(String.format("Candidate Fit Score: %.1f%%\n\n", score * 100));
        sb.append("WHY THIS CANDIDATE (Your Perspective):\n");
        sb.append(String.format("  Candidate meets %d of %d required skill criteria.\n",
            matched.size(), matched.size() + missing.size()));

        if (!matched.isEmpty()) {
            sb.append("  Covered Requirements: ").append(String.join(", ", matched)).append("\n");
        }
        if (!missing.isEmpty()) {
            sb.append("  Skill Gaps: ").append(String.join(", ", missing)).append("\n");
            sb.append("  These could be addressed through onboarding or mentoring.\n");
        }

        sb.append(String.format("\n  Candidate Availability: %d weeks (your requirement: %d weeks).\n",
            applicant.getAvailabilityWeeks(), recruiter.getDurationWeeks()));

        sb.append("\nRECOMMENDATION: ");
        if (score >= 0.75) {
            sb.append("Highly recommended. Prioritise this candidate for interview.");
        } else if (score >= 0.50) {
            sb.append("Viable candidate. Review their profile and consider a screening call.");
        } else {
            sb.append("Low fit. Consider only if no stronger candidates are available.");
        }
        return sb.toString();
    }

    @Override
    public String getStrategyName() {
        return "Weighted Skill-Graph Strategy";
    }
}
