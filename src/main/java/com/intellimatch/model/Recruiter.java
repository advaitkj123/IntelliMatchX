package com.intellimatch.model;

/**
 * Represents a recruiter posting an internship opportunity.
 */
public class Recruiter extends Person {

    private String company;
    private String internshipTitle;
    private int durationWeeks;

    public Recruiter(String name, String email, String company, String internshipTitle, int durationWeeks) {
        super(name, email);
        this.company = company;
        this.internshipTitle = internshipTitle;
        this.durationWeeks = durationWeeks;
    }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getInternshipTitle() { return internshipTitle; }
    public void setInternshipTitle(String internshipTitle) { this.internshipTitle = internshipTitle; }

    public int getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(int durationWeeks) { this.durationWeeks = durationWeeks; }

    @Override
    public String getRole() { return "Recruiter"; }

    public String getSummary() {
        return String.format(
            "Recruiter: %s | Company: %s | Role: %s | Duration: %d weeks | Required Skills: %s",
            name, company, internshipTitle, durationWeeks, skills
        );
    }
}
