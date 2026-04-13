package com.intellimatch.model;

/**
 * Represents a recruiter posting an internship opportunity.
 */
public class Recruiter extends Person {

    private String company;
    private String internshipTitle;
    private int durationWeeks;
    private String roleLevel;
    private String location;
    private String stipend;
    private String startDate;
    private String workMode;

    public Recruiter(String name, String email, String company, String internshipTitle, int durationWeeks) {
        this(name, email, company, internshipTitle, durationWeeks,
            "Intern", "Not specified", "Not specified", "Flexible", "Hybrid");
    }

    public Recruiter(
            String name,
            String email,
            String company,
            String internshipTitle,
            int durationWeeks,
            String roleLevel,
            String location,
            String stipend,
            String startDate,
            String workMode) {
        super(name, email);
        this.company = company;
        this.internshipTitle = internshipTitle;
        this.durationWeeks = durationWeeks;
        this.roleLevel = roleLevel;
        this.location = location;
        this.stipend = stipend;
        this.startDate = startDate;
        this.workMode = workMode;
    }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getInternshipTitle() { return internshipTitle; }
    public void setInternshipTitle(String internshipTitle) { this.internshipTitle = internshipTitle; }

    public int getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(int durationWeeks) { this.durationWeeks = durationWeeks; }

    public String getRoleLevel() { return roleLevel; }
    public void setRoleLevel(String roleLevel) { this.roleLevel = roleLevel; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStipend() { return stipend; }
    public void setStipend(String stipend) { this.stipend = stipend; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    @Override
    public String getRole() { return "Recruiter"; }

    public String getSummary() {
        return String.format(
            "Recruiter: %s | Company: %s | Role: %s | Level: %s | Mode: %s | Location: %s | Duration: %d weeks | Required Skills: %s",
            name, company, internshipTitle, roleLevel, workMode, location, durationWeeks, skills
        );
    }
}
