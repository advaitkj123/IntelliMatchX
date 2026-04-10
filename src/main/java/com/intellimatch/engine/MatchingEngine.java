package com.intellimatch.engine;

import com.intellimatch.model.Applicant;
import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;
import com.intellimatch.observer.MatchEventBus;
import com.intellimatch.strategy.MatchingStrategy;
import com.intellimatch.strategy.WeightedSkillGraphStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Core bi-directional matching engine.
 *
 * Uses the Strategy Pattern to delegate to pluggable matching algorithms.
 * Uses the Observer Pattern via MatchEventBus to broadcast match events.
 *
 * Bi-directional: given a list of applicants and a list of recruiters,
 * it computes:
 *   - For each recruiter: ranked list of best applicants (talent discovery)
 *   - For each applicant: ranked list of best opportunities (recommendations)
 */
public class MatchingEngine {

    private static final Logger LOG = Logger.getLogger(MatchingEngine.class.getName());

    private MatchingStrategy strategy;
    private final MatchEventBus eventBus;

    public MatchingEngine() {
        this.strategy = new WeightedSkillGraphStrategy();
        this.eventBus = MatchEventBus.getInstance();
    }

    public MatchingEngine(MatchingStrategy strategy) {
        this.strategy = strategy;
        this.eventBus = MatchEventBus.getInstance();
    }

    public void setStrategy(MatchingStrategy strategy) {
        this.strategy = strategy;
        LOG.info("Matching strategy changed to: " + strategy.getStrategyName());
    }

    public MatchingStrategy getStrategy() { return strategy; }

    /**
     * Compute a single match between one applicant and one recruiter.
     * Publishes the result via the event bus.
     */
    public MatchResult matchOne(Applicant applicant, Recruiter recruiter) {
        MatchResult result = strategy.match(applicant, recruiter);
        eventBus.publish(result);
        return result;
    }

    /**
     * Bi-directional mode: compute all applicant-recruiter pair scores.
     * Publishes each result via the event bus.
     *
     * @return all MatchResults sorted by descending score
     */
    public List<MatchResult> matchAll(List<Applicant> applicants, List<Recruiter> recruiters) {
        List<MatchResult> results = new ArrayList<>();

        for (Applicant applicant : applicants) {
            for (Recruiter recruiter : recruiters) {
                MatchResult result = strategy.match(applicant, recruiter);
                results.add(result);
                eventBus.publish(result);
            }
        }

        results.sort(Comparator.comparingDouble(MatchResult::getScore).reversed());
        LOG.info("Bi-directional matching complete. Total pairs evaluated: " + results.size());
        return results;
    }

    /**
     * Applicant discovery view: returns top recruiters for a given applicant.
     */
    public List<MatchResult> getRecommendationsForApplicant(Applicant applicant, List<Recruiter> recruiters, int topN) {
        List<MatchResult> results = new ArrayList<>();
        for (Recruiter recruiter : recruiters) {
            results.add(strategy.match(applicant, recruiter));
        }
        results.sort(Comparator.comparingDouble(MatchResult::getScore).reversed());
        return results.subList(0, Math.min(topN, results.size()));
    }

    /**
     * Recruiter discovery view: returns top applicants for a given recruiter.
     */
    public List<MatchResult> getTopCandidatesForRecruiter(Recruiter recruiter, List<Applicant> applicants, int topN) {
        List<MatchResult> results = new ArrayList<>();
        for (Applicant applicant : applicants) {
            results.add(strategy.match(applicant, recruiter));
        }
        results.sort(Comparator.comparingDouble(MatchResult::getScore).reversed());
        return results.subList(0, Math.min(topN, results.size()));
    }
}
