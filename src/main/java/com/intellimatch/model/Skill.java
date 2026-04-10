package com.intellimatch.model;

import java.util.Objects;

/**
 * Represents a skill with a name and a proficiency weight (0.0 - 1.0).
 */
public class Skill {

    private final String name;
    private final double weight;

    public Skill(String name, double weight) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Skill name cannot be blank");
        if (weight < 0.0 || weight > 1.0) throw new IllegalArgumentException("Weight must be between 0.0 and 1.0");
        this.name = name.toLowerCase().trim();
        this.weight = weight;
    }

    public String getName() { return name; }
    public double getWeight() { return weight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill)) return false;
        Skill skill = (Skill) o;
        return Objects.equals(name, skill.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }

    @Override
    public String toString() { return name + "(w=" + String.format("%.2f", weight) + ")"; }
}
