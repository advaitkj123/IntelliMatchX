package com.intellimatch.engine;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utility for exact/similar skill detection used by candidate skill filtering.
 */
public class SkillSimilarityMatcher {

    private static final Map<String, Set<String>> SKILL_SYNONYMS = Map.ofEntries(
        Map.entry("js", Set.of("javascript")),
        Map.entry("javascript", Set.of("js", "typescript")),
        Map.entry("typescript", Set.of("javascript", "ts")),
        Map.entry("ts", Set.of("typescript")),
        Map.entry("ml", Set.of("machine learning")),
        Map.entry("machine learning", Set.of("ml", "ai")),
        Map.entry("ai", Set.of("machine learning")),
        Map.entry("spring", Set.of("spring boot")),
        Map.entry("spring boot", Set.of("spring")),
        Map.entry("k8s", Set.of("kubernetes")),
        Map.entry("kubernetes", Set.of("k8s"))
    );

    public boolean isExactMatch(String candidateSkill, String requiredSkill) {
        return normalize(candidateSkill).equals(normalize(requiredSkill));
    }

    public boolean isSimilarMatch(String candidateSkill, String requiredSkill) {
        String left = normalize(candidateSkill);
        String right = normalize(requiredSkill);

        if (left.equals(right)) {
            return false;
        }

        if (isSynonym(left, right)) {
            return true;
        }

        if (left.contains(right) || right.contains(left)) {
            return true;
        }

        return tokenOverlapRatio(left, right) >= 0.5;
    }

    private boolean isSynonym(String left, String right) {
        return SKILL_SYNONYMS.getOrDefault(left, Set.of()).contains(right)
            || SKILL_SYNONYMS.getOrDefault(right, Set.of()).contains(left);
    }

    private double tokenOverlapRatio(String left, String right) {
        List<String> leftTokens = List.of(left.split("\\s+"));
        List<String> rightTokens = List.of(right.split("\\s+"));
        long overlap = leftTokens.stream().filter(rightTokens::contains).count();
        int maxSize = Math.max(leftTokens.size(), rightTokens.size());
        return maxSize == 0 ? 0.0 : (double) overlap / maxSize;
    }

    private String normalize(String skill) {
        return skill == null ? "" : skill.trim().toLowerCase(Locale.ROOT);
    }
}
