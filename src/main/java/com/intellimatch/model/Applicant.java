package com.intellimatch.model;

import java.util.List;

/**
 * Represents an internship applicant (job seeker).
 */
public class Applicant extends Person {

    private String desiredRole;
    private int availabilityWeeks;

    public Applicant(String name, String email, String desiredRole, int availabilityWeeks) {
        super(name, email);
        this.desiredRole = desiredRole;
        this.availabilityWeeks = availabilityWeeks;
    }

    public String getDesiredRole() { return desiredRole; }
    public void setDesiredRole(String desiredRole) { this.desiredRole = desiredRole; }

    public int getAvailabilityWeeks() { return availabilityWeeks; }
    public void setAvailabilityWeeks(int availabilityWeeks) { this.availabilityWeeks = availabilityWeeks; }

    @Override
    public String getRole() { return "Applicant"; }

    public String getSummary() {
        return String.format(
            "Name: %s | Role Sought: %s | Availability: %d weeks | Skills: %s",
            name, desiredRole, availabilityWeeks, skills
        );
    }
}
