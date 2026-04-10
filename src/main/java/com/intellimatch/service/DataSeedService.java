package com.intellimatch.service;

import com.intellimatch.factory.PersonFactory;
import com.intellimatch.model.Applicant;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides sample data for demonstration purposes.
 * Seeds the application with realistic applicants and recruiters.
 */
public class DataSeedService {

    public List<Applicant> seedApplicants() {
        List<Applicant> applicants = new ArrayList<>();

        applicants.add(PersonFactory.createApplicant(
            "Alice Chen", "alice@example.com", "Software Engineering Intern", 12,
            List.of(
                new Skill("java", 0.9),
                new Skill("spring boot", 0.8),
                new Skill("sql", 0.75),
                new Skill("git", 0.85),
                new Skill("docker", 0.6)
            )
        ));

        applicants.add(PersonFactory.createApplicant(
            "Bob Martinez", "bob@example.com", "Data Science Intern", 16,
            List.of(
                new Skill("python", 0.9),
                new Skill("machine learning", 0.85),
                new Skill("sql", 0.8),
                new Skill("pandas", 0.75),
                new Skill("numpy", 0.7),
                new Skill("tensorflow", 0.65)
            )
        ));

        applicants.add(PersonFactory.createApplicant(
            "Clara Johnson", "clara@example.com", "Frontend Developer Intern", 10,
            List.of(
                new Skill("react", 0.9),
                new Skill("javascript", 0.95),
                new Skill("typescript", 0.8),
                new Skill("css", 0.85),
                new Skill("html", 1.0),
                new Skill("git", 0.7)
            )
        ));

        applicants.add(PersonFactory.createApplicant(
            "David Kim", "david@example.com", "Cloud & DevOps Intern", 14,
            List.of(
                new Skill("kubernetes", 0.8),
                new Skill("docker", 0.9),
                new Skill("aws", 0.85),
                new Skill("terraform", 0.7),
                new Skill("linux", 0.9),
                new Skill("python", 0.6)
            )
        ));

        applicants.add(PersonFactory.createApplicant(
            "Eva Patel", "eva@example.com", "Full Stack Intern", 12,
            List.of(
                new Skill("java", 0.75),
                new Skill("react", 0.8),
                new Skill("javascript", 0.85),
                new Skill("sql", 0.7),
                new Skill("spring boot", 0.65),
                new Skill("git", 0.9)
            )
        ));

        return applicants;
    }

    public List<Recruiter> seedRecruiters() {
        List<Recruiter> recruiters = new ArrayList<>();

        recruiters.add(PersonFactory.createRecruiter(
            "Sarah Lee", "sarah@techcorp.com", "TechCorp Inc.", "Backend Software Engineering Intern", 12,
            List.of(
                new Skill("java", 0.95),
                new Skill("spring boot", 0.9),
                new Skill("sql", 0.8),
                new Skill("docker", 0.7),
                new Skill("git", 0.75)
            )
        ));

        recruiters.add(PersonFactory.createRecruiter(
            "James Wong", "james@datavision.io", "DataVision AI", "Machine Learning Intern", 16,
            List.of(
                new Skill("python", 0.95),
                new Skill("machine learning", 0.9),
                new Skill("tensorflow", 0.85),
                new Skill("pandas", 0.8),
                new Skill("sql", 0.7)
            )
        ));

        recruiters.add(PersonFactory.createRecruiter(
            "Linda Brooks", "linda@webcraft.dev", "WebCraft Studios", "React Frontend Intern", 10,
            List.of(
                new Skill("react", 0.95),
                new Skill("typescript", 0.9),
                new Skill("javascript", 0.85),
                new Skill("css", 0.8),
                new Skill("git", 0.7)
            )
        ));

        recruiters.add(PersonFactory.createRecruiter(
            "Mark Nguyen", "mark@cloudnine.io", "CloudNine Systems", "DevOps & Cloud Intern", 14,
            List.of(
                new Skill("kubernetes", 0.9),
                new Skill("docker", 0.85),
                new Skill("aws", 0.95),
                new Skill("terraform", 0.8),
                new Skill("linux", 0.85)
            )
        ));

        return recruiters;
    }
}
