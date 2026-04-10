package com.intellimatch.strategy;

import com.intellimatch.model.Applicant;
import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strategy Pattern - Concrete: Exact Skill Count Match Algorithm.
 *
 * Simpler strategy: score = exact matches / total required skills (no weights).
 * Useful for strict role requirements.
 */
public class ExactMatchStrategy implements MatchingStrategy {

    @Override
    public MatchResult match(Applicant applicant, Recruiter recruiter) {
        Set<String> applicantSkillNames = applicant.getSkills().stream()
            .map(Skill::getName)
            .collect(Collectors.toSet());

        List<String> requiredNames = recruiter.getSkills().stream()
            .map(Skill::getName)
            .collect(Collectors.toList());

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String required : requiredNames) {
            if (applicantSkillNames.contains(required)) {
                matched.add(required);
            } else {
                missing.add(required);
            }
        }

        double score = requiredNames.isEmpty() ? 0.0 : (double) matched.size() / requiredNames.size();

        String applicantView = String.format(
            "Exact Match Analysis for %s:\nScore: %.1f%%\nMatched: %s\nMissing: %s",
            applicant.getName(), score * 100, matched, missing
        );
        String recruiterView = String.format(
            "Candidate %s — Exact Fit: %.1f%%\nCovered: %s\nGaps: %s",
            applicant.getName(), score * 100, matched, missing
        );

        return new MatchResult(applicant, recruiter, score, matched, missing, applicantView, recruiterView);
    }

    @Override
    public String getStrategyName() {
        return "Exact Skill Count Strategy";
    }
}
