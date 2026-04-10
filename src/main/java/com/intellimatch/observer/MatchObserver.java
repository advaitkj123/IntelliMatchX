package com.intellimatch.observer;

import com.intellimatch.model.MatchResult;

/**
 * Observer Pattern: defines the contract for entities that receive match notifications.
 */
public interface MatchObserver {

    /**
     * Called when a new match has been computed.
     *
     * @param result the computed MatchResult
     */
    void onMatchFound(MatchResult result);
}
