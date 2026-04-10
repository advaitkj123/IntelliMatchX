package com.intellimatch.observer;

import com.intellimatch.model.MatchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Observer Pattern: a publish-subscribe event bus that broadcasts MatchResult events
 * to all registered MatchObserver listeners in real-time.
 */
public class MatchEventBus {

    private static final Logger LOG = Logger.getLogger(MatchEventBus.class.getName());
    private static MatchEventBus instance;

    private final List<MatchObserver> observers = new ArrayList<>();

    private MatchEventBus() {}

    public static synchronized MatchEventBus getInstance() {
        if (instance == null) {
            instance = new MatchEventBus();
        }
        return instance;
    }

    public void subscribe(MatchObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            LOG.info("Observer registered: " + observer.getClass().getSimpleName());
        }
    }

    public void unsubscribe(MatchObserver observer) {
        observers.remove(observer);
    }

    public void publish(MatchResult result) {
        LOG.info("Broadcasting match event: " + result);
        for (MatchObserver observer : observers) {
            try {
                observer.onMatchFound(result);
            } catch (Exception e) {
                LOG.warning("Observer " + observer.getClass().getSimpleName() + " threw an exception: " + e.getMessage());
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}
