package com.intellimatch.factory;

import com.intellimatch.model.Applicant;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.Skill;

import java.util.List;

/**
 * Factory Pattern: creates Applicant and Recruiter entities with their associated skills.
 * Centralizes object construction to decouple creation logic from business logic.
 */
public class PersonFactory {

    private PersonFactory() {}

    /**
     * Creates an Applicant and populates it with the provided skills.
     */
    public static Applicant createApplicant(
            String name,
            String email,
            String desiredRole,
            int availabilityWeeks,
            List<Skill> skills) {
        Applicant applicant = new Applicant(name, email, desiredRole, availabilityWeeks);
        if (skills != null) {
            skills.forEach(applicant::addSkill);
        }
        return applicant;
    }

    /**
     * Creates a Recruiter and populates it with the required skills for the posting.
     */
    public static Recruiter createRecruiter(
            String name,
            String email,
            String company,
            String internshipTitle,
            int durationWeeks,
            List<Skill> requiredSkills) {
        Recruiter recruiter = new Recruiter(name, email, company, internshipTitle, durationWeeks);
        if (requiredSkills != null) {
            requiredSkills.forEach(recruiter::addSkill);
        }
        return recruiter;
    }

    /**
     * Convenience builder for Skill creation.
     */
    public static Skill createSkill(String name, double weight) {
        return new Skill(name, weight);
    }
}
