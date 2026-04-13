package com.intellimatch.service;

import com.intellimatch.factory.PersonFactory;
import com.intellimatch.model.Applicant;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides larger sample data to simulate a realistic ecosystem.
 * Includes 50 recruiters from real-world companies and 50 diverse candidates.
 */
public class DataSeedService {

    public List<Applicant> seedApplicants() {
        List<ApplicantBlueprint> blueprints = applicantBlueprints();
        List<String> names = candidateNames();
        List<Applicant> applicants = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            ApplicantBlueprint blueprint = blueprints.get(i % blueprints.size());
            String name = names.get(i);
            String email = String.format(Locale.ROOT, "candidate%d@maildemo.com", i + 1);
            int availability = 10 + (i % 8);
            List<Skill> skills = buildWeightedSkills(blueprint.skills(), i, 0.56);

            applicants.add(PersonFactory.createApplicant(
                name,
                email,
                blueprint.background(),
                availability,
                skills
            ));
        }
        return applicants;
    }

    public List<Recruiter> seedRecruiters() {
        List<String> companies = companyNames();
        List<String> recruiterNames = recruiterNames();
        List<RecruiterBlueprint> blueprints = recruiterBlueprints();
        List<Recruiter> recruiters = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            RecruiterBlueprint blueprint = blueprints.get(i % blueprints.size());
            String company = companies.get(i);
            String recruiterName = recruiterNames.get(i);
            String email = String.format(Locale.ROOT, "talent%d@%s.com", i + 1, slug(company));
            int duration = 10 + ((i % 4) * 2);
            List<Skill> requiredSkills = buildWeightedSkills(blueprint.skills(), i, 0.68);

            recruiters.add(PersonFactory.createRecruiter(
                recruiterName,
                email,
                company,
                blueprint.internshipTitle(),
                duration,
                requiredSkills
            ));
        }
        return recruiters;
    }

    private List<Skill> buildWeightedSkills(List<String> names, int seed, double base) {
        List<Skill> weighted = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            double weight = Math.min(0.95, base + (((seed + i) % 8) * 0.05));
            weighted.add(new Skill(names.get(i), weight));
        }
        return weighted;
    }

    private String slug(String company) {
        String normalized = company.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
        return normalized.isBlank() ? "company" : normalized;
    }

    private List<String> companyNames() {
        return List.of(
            "Google", "Microsoft", "Amazon", "Apple", "Meta", "Netflix", "Adobe", "Salesforce", "Oracle", "IBM",
            "Intel", "NVIDIA", "Qualcomm", "Cisco", "SAP", "Accenture", "Deloitte", "PwC", "Goldman Sachs", "JPMorgan Chase",
            "Morgan Stanley", "Uber", "Airbnb", "Stripe", "PayPal", "Spotify", "Atlassian", "Shopify", "VMware", "ServiceNow",
            "Snowflake", "Databricks", "Palantir", "Tesla", "SpaceX", "Samsung", "Siemens", "Bosch", "Shell", "Unilever",
            "Procter & Gamble", "The Coca-Cola Company", "PepsiCo", "Nike", "Walmart", "Target", "Booking.com", "ByteDance", "Zoom", "LinkedIn"
        );
    }

    private List<String> recruiterNames() {
        return List.of(
            "Sarah Lee", "James Wong", "Linda Brooks", "Mark Nguyen", "Ava Thompson", "Noah Patel", "Mia Rodriguez", "Ethan Kim", "Sofia Ahmed", "Liam Carter",
            "Olivia Scott", "Mason Hall", "Isabella Young", "Lucas Turner", "Amelia Collins", "Elijah Foster", "Charlotte Reed", "Harper Bell", "Henry Ward", "Evelyn Simmons",
            "Benjamin Cooper", "Abigail Morris", "Logan Powell", "Emily Kelly", "Daniel Barnes", "Grace Rivera", "Matthew Price", "Chloe Bennett", "Sebastian Ross", "Lily Jenkins",
            "Aiden Hughes", "Scarlett Fisher", "Jacob Sanders", "Aria Bailey", "Michael Perry", "Nora Long", "David Cox", "Ella Howard", "Joseph Myers", "Zoey Watson",
            "Samuel Brooks", "Hannah Griffin", "Owen Russell", "Avery Diaz", "Levi Hayes", "Penelope Foster", "Wyatt Graham", "Riley Stone", "Julian Cooper", "Layla Price"
        );
    }

    private List<String> candidateNames() {
        return List.of(
            "Aarav Sharma", "Emma Wilson", "Noah Garcia", "Sophia Brown", "Ishan Mehta", "Maya Singh", "Rahul Verma", "Aisha Khan", "Kunal Desai", "Priya Nair",
            "Arjun Rao", "Neha Kapoor", "Kabir Joshi", "Saanvi Iyer", "Rohan Banerjee", "Ananya Das", "Vihaan Menon", "Zara Ali", "Aryan Malhotra", "Ira Chawla",
            "Leo Martin", "Mila Anderson", "Omar Hassan", "Nina D'Souza", "Aditya Kulkarni", "Sara Thomas", "Rajat Bhatia", "Ishita Gupta", "Devansh Arora", "Tara Mishra",
            "Aiden Parker", "Isla Morgan", "Yusuf Rahman", "Kiara Sen", "Nikhil Jain", "Pooja Sethi", "Reyansh Roy", "Meera Pillai", "Veer Khurana", "Riya Patel",
            "Ethan Reed", "Ava Miller", "Hugo Fernandes", "Anika Bose", "Siddharth Ghosh", "Fatima Noor", "Krish Bansal", "Lavanya Reddy", "Ryan Campbell", "Aanya Bhatt"
        );
    }

    private List<RecruiterBlueprint> recruiterBlueprints() {
        return List.of(
            new RecruiterBlueprint("Backend Engineering Intern", List.of("java", "spring boot", "sql", "docker", "git")),
            new RecruiterBlueprint("Machine Learning Intern", List.of("python", "machine learning", "pandas", "tensorflow", "sql")),
            new RecruiterBlueprint("Frontend Engineering Intern", List.of("react", "javascript", "typescript", "css", "git")),
            new RecruiterBlueprint("DevOps & Cloud Intern", List.of("aws", "kubernetes", "docker", "terraform", "linux")),
            new RecruiterBlueprint("Data Engineering Intern", List.of("python", "spark", "sql", "airflow", "aws")),
            new RecruiterBlueprint("Mobile App Intern", List.of("kotlin", "android", "rest api", "git", "firebase")),
            new RecruiterBlueprint("Security Engineering Intern", List.of("network security", "linux", "python", "siem", "cloud security")),
            new RecruiterBlueprint("Product Analytics Intern", List.of("sql", "tableau", "python", "statistics", "excel")),
            new RecruiterBlueprint("Full Stack Intern", List.of("java", "react", "spring boot", "javascript", "sql")),
            new RecruiterBlueprint("QA Automation Intern", List.of("selenium", "java", "api testing", "testng", "git"))
        );
    }

    private List<ApplicantBlueprint> applicantBlueprints() {
        return List.of(
            new ApplicantBlueprint("Computer Science Undergraduate - Backend Focus", List.of("java", "spring boot", "sql", "git", "docker")),
            new ApplicantBlueprint("Statistics Graduate - Data Science Track", List.of("python", "machine learning", "pandas", "numpy", "sql")),
            new ApplicantBlueprint("Design Student Transitioning to Frontend Engineering", List.of("react", "javascript", "css", "html", "figma")),
            new ApplicantBlueprint("Mechanical Engineer Upskilling in Cloud DevOps", List.of("aws", "linux", "docker", "terraform", "python")),
            new ApplicantBlueprint("Finance Graduate Learning Product Analytics", List.of("sql", "excel", "tableau", "python", "statistics")),
            new ApplicantBlueprint("Electronics Student Interested in Embedded & Mobile", List.of("c++", "kotlin", "android", "git", "rest api")),
            new ApplicantBlueprint("Cybersecurity Enthusiast with Lab Experience", List.of("network security", "linux", "python", "wireshark", "cloud security")),
            new ApplicantBlueprint("Bootcamp Graduate - Full Stack JavaScript", List.of("node.js", "react", "javascript", "mongodb", "git")),
            new ApplicantBlueprint("Mathematics Major Moving into AI Engineering", List.of("python", "machine learning", "tensorflow", "statistics", "sql")),
            new ApplicantBlueprint("Information Systems Student - QA Automation", List.of("selenium", "java", "api testing", "postman", "git"))
        );
    }

    private record RecruiterBlueprint(String internshipTitle, List<String> skills) {}
    private record ApplicantBlueprint(String background, List<String> skills) {}
}
