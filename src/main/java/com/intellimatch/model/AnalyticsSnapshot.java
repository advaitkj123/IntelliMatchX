package com.intellimatch.model;

import java.util.Map;

public class AnalyticsSnapshot {

    private final int signedUpUsers;
    private final int activeUsers;
    private final int shortlistedCandidates;
    private final int totalCandidates;
    private final int totalRecruiters;
    private final Map<String, Long> topCandidateSkills;
    private final Map<String, Long> marketDemandSkills;
    private final Map<String, Long> workModeDemand;

    public AnalyticsSnapshot(
            int signedUpUsers,
            int activeUsers,
            int shortlistedCandidates,
            int totalCandidates,
            int totalRecruiters,
            Map<String, Long> topCandidateSkills,
            Map<String, Long> marketDemandSkills,
            Map<String, Long> workModeDemand) {
        this.signedUpUsers = signedUpUsers;
        this.activeUsers = activeUsers;
        this.shortlistedCandidates = shortlistedCandidates;
        this.totalCandidates = totalCandidates;
        this.totalRecruiters = totalRecruiters;
        this.topCandidateSkills = Map.copyOf(topCandidateSkills);
        this.marketDemandSkills = Map.copyOf(marketDemandSkills);
        this.workModeDemand = Map.copyOf(workModeDemand);
    }

    public int getSignedUpUsers() { return signedUpUsers; }
    public int getActiveUsers() { return activeUsers; }
    public int getShortlistedCandidates() { return shortlistedCandidates; }
    public int getTotalCandidates() { return totalCandidates; }
    public int getTotalRecruiters() { return totalRecruiters; }
    public Map<String, Long> getTopCandidateSkills() { return topCandidateSkills; }
    public Map<String, Long> getMarketDemandSkills() { return marketDemandSkills; }
    public Map<String, Long> getWorkModeDemand() { return workModeDemand; }
}
