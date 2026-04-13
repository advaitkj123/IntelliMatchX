package com.intellimatch.service;

import com.intellimatch.model.CompanySkillMatchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingServiceCandidateSkillSearchTest {

    private final MatchingService service = new MatchingService();

    @Test
    void shouldPrioritizeTechCorpForBackendSkills() {
        List<CompanySkillMatchResult> results = service.findCompaniesByCandidateSkills(
            "Tanay",
            List.of("java", "spring boot", "sql"),
            5
        );

        assertFalse(results.isEmpty());
        assertEquals("TechCorp Inc.", results.get(0).getCompany());
        assertTrue(results.get(0).getExactMatchedSkills().contains("java"));
        assertTrue(results.get(0).getScore() > 0.0);
    }

    @Test
    void shouldTreatSynonymsAsSimilarMatches() {
        List<CompanySkillMatchResult> results = service.findCompaniesByCandidateSkills(
            "Tanay",
            List.of("js"),
            10
        );

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> !r.getSimilarMatchedSkills().isEmpty()));
    }

    @Test
    void shouldRejectBlankCandidateName() {
        assertThrows(IllegalArgumentException.class, () ->
            service.findCompaniesByCandidateSkills("   ", List.of("java"), 5)
        );
    }

    @Test
    void shouldRejectEmptySkillList() {
        assertThrows(IllegalArgumentException.class, () ->
            service.findCompaniesByCandidateSkills("Tanay", List.of(), 5)
        );
    }
}
