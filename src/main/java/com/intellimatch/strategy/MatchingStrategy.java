package com.intellimatch.strategy;

import com.intellimatch.model.Applicant;
import com.intellimatch.model.MatchResult;
import com.intellimatch.model.Recruiter;

/**
 * Strategy Pattern: defines the matching algorithm contract.
 * Allows swapping matching strategies at runtime.
 */
public interface MatchingStrategy {

    /**
     * Computes a bi-directional match between an applicant and a recruiter.
     *
     * @param applicant the job seeker
     * @param recruiter the opportunity poster
     * @return a MatchResult containing score and explainability data
     */
    MatchResult match(Applicant applicant, Recruiter recruiter);

    /**
     * Returns the human-readable name of this strategy.
     */
    String getStrategyName();
}
