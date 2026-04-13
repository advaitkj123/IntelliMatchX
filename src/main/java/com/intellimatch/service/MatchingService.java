package com.intellimatch.service;

import com.intellimatch.engine.MatchingEngine;
import com.intellimatch.model.Applicant;
import com.intellimatch.model.CompanySkillMatchResult;
import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.observer.MatchEventBus;
import com.intellimatch.observer.NotificationLogger;
import com.intellimatch.strategy.ExactMatchStrategy;
import com.intellimatch.strategy.MatchingStrategy;
import com.intellimatch.strategy.WeightedSkillGraphStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Service facade that wires together the engine, observers, and data seed.
 * Acts as the application-layer boundary for UI controllers.
 */
public class MatchingService {

    private final MatchingEngine engine;
    private final NotificationLogger notificationLogger;
    private final DatabaseService databaseService;
    private List<Applicant> applicants;
    private List<Recruiter> recruiters;

    public MatchingService() {
        this.engine = new MatchingEngine();
        this.notificationLogger = new NotificationLogger();
        this.databaseService = DatabaseService.getInstance();

        MatchEventBus.getInstance().subscribe(notificationLogger);
        refreshData();
    }

    public List<MatchResult> runFullBidirectionalMatch() {
        refreshData();
        notificationLogger.clearFeed();
        return engine.matchAll(applicants, recruiters);
    }

    public List<MatchResult> getRecommendationsForApplicant(Applicant applicant, int topN) {
        refreshData();
        return engine.getRecommendationsForApplicant(applicant, recruiters, topN);
    }

    public List<MatchResult> getTopCandidatesForRecruiter(Recruiter recruiter, int topN) {
        refreshData();
        return engine.getTopCandidatesForRecruiter(recruiter, applicants, topN);
    }

    /**
     * New backend endpoint-style method:
     * candidate provides name and key skills, and receives ranked company matches
     * based on exact and similar skill requirements.
     */
    public List<CompanySkillMatchResult> findCompaniesByCandidateSkills(
            String candidateName,
            List<String> keySkills,
            int topN) {
        refreshData();
        if (candidateName == null || candidateName.isBlank()) {
            throw new IllegalArgumentException("Candidate name cannot be blank");
        }
        if (keySkills == null || keySkills.isEmpty()) {
            throw new IllegalArgumentException("At least one key skill is required");
        }

        List<String> cleanedSkills = keySkills.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(skill -> !skill.isBlank())
            .toList();

        if (cleanedSkills.isEmpty()) {
            throw new IllegalArgumentException("At least one non-blank key skill is required");
        }

        int boundedTopN = Math.max(1, topN);
        return engine.findCompaniesForCandidateSkills(candidateName.trim(), cleanedSkills, recruiters, boundedTopN);
    }

    public void setWeightedStrategy() {
        engine.setStrategy(new WeightedSkillGraphStrategy());
    }

    public void setExactMatchStrategy() {
        engine.setStrategy(new ExactMatchStrategy());
    }

    public void setCustomStrategy(MatchingStrategy strategy) {
        engine.setStrategy(strategy);
    }

    public List<Applicant> getApplicants() {
        refreshData();
        return applicants;
    }

    public List<Recruiter> getRecruiters() {
        refreshData();
        return recruiters;
    }

    public NotificationLogger getNotificationLogger() { return notificationLogger; }
    public MatchingEngine getEngine() { return engine; }

    public void addApplicant(Applicant applicant) {
        databaseService.addApplicantProfile(applicant);
        refreshData();
    }

    public void addRecruiter(Recruiter recruiter) {
        databaseService.addRecruiterProfile(recruiter);
        refreshData();
    }

    private void refreshData() {
        this.applicants = databaseService.getAllApplicants();
        this.recruiters = databaseService.getAllRecruiters();
    }
}
