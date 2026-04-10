package com.intellimatch.service;

import com.intellimatch.engine.MatchingEngine;
import com.intellimatch.model.Applicant;
import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.observer.MatchEventBus;
import com.intellimatch.observer.NotificationLogger;
import com.intellimatch.strategy.ExactMatchStrategy;
import com.intellimatch.strategy.MatchingStrategy;
import com.intellimatch.strategy.WeightedSkillGraphStrategy;

import java.util.List;

/**
 * Service facade that wires together the engine, observers, and data seed.
 * Acts as the application-layer boundary for UI controllers.
 */
public class MatchingService {

    private final MatchingEngine engine;
    private final NotificationLogger notificationLogger;
    private final DataSeedService seedService;
    private List<Applicant> applicants;
    private List<Recruiter> recruiters;

    public MatchingService() {
        this.engine = new MatchingEngine();
        this.notificationLogger = new NotificationLogger();
        this.seedService = new DataSeedService();

        MatchEventBus.getInstance().subscribe(notificationLogger);

        this.applicants = seedService.seedApplicants();
        this.recruiters = seedService.seedRecruiters();
    }

    public List<MatchResult> runFullBidirectionalMatch() {
        notificationLogger.clearFeed();
        return engine.matchAll(applicants, recruiters);
    }

    public List<MatchResult> getRecommendationsForApplicant(Applicant applicant, int topN) {
        return engine.getRecommendationsForApplicant(applicant, recruiters, topN);
    }

    public List<MatchResult> getTopCandidatesForRecruiter(Recruiter recruiter, int topN) {
        return engine.getTopCandidatesForRecruiter(recruiter, applicants, topN);
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

    public List<Applicant> getApplicants() { return applicants; }
    public List<Recruiter> getRecruiters() { return recruiters; }
    public NotificationLogger getNotificationLogger() { return notificationLogger; }
    public MatchingEngine getEngine() { return engine; }

    public void addApplicant(Applicant applicant) {
        applicants.add(applicant);
    }

    public void addRecruiter(Recruiter recruiter) {
        recruiters.add(recruiter);
    }
}
