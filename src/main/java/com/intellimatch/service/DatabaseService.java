package com.intellimatch.service;

import com.intellimatch.factory.PersonFactory;
import com.intellimatch.model.Applicant;
import com.intellimatch.model.Recruiter;
import com.intellimatch.model.Skill;
import com.intellimatch.model.UserAccount;
import com.intellimatch.model.UserRole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lightweight local persistent database backed by flat files.
 * Stores user accounts, candidate profiles, and recruiter profiles.
 */
public class DatabaseService {

    private static final DatabaseService INSTANCE = new DatabaseService();
    private static final String DEFAULT_SEED_PASSWORD = "Password@123";

    private final Path dataDir = Path.of("data");
    private final Path usersFile = dataDir.resolve("users.tsv");
    private final Path candidatesFile = dataDir.resolve("candidates.tsv");
    private final Path recruitersFile = dataDir.resolve("recruiters.tsv");

    private final Map<String, UserAccount> accountsByEmail = new LinkedHashMap<>();
    private final Map<String, Applicant> candidatesByEmail = new LinkedHashMap<>();
    private final Map<String, Recruiter> recruitersByEmail = new LinkedHashMap<>();

    private final DataSeedService seedService = new DataSeedService();

    private DatabaseService() {
        initialize();
    }

    public static DatabaseService getInstance() {
        return INSTANCE;
    }

    public String getDefaultSeedPassword() {
        return DEFAULT_SEED_PASSWORD;
    }

    public synchronized Optional<UserAccount> authenticate(String email, String password, UserRole role) {
        String normalizedEmail = normalizeEmail(email);
        UserAccount account = accountsByEmail.get(normalizedEmail);
        if (account == null || account.getRole() != role) {
            return Optional.empty();
        }
        if (!account.getPasswordHash().equals(hashPassword(password))) {
            return Optional.empty();
        }
        return Optional.of(account);
    }

    public synchronized UserAccount registerCandidate(
            String name,
            String email,
            String password,
            String background,
            List<String> skills) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Candidate name is required.");
        }
        if (skills == null || skills.isEmpty()) {
            throw new IllegalArgumentException("At least one skill is required.");
        }

        String normalizedEmail = normalizeEmail(email);
        ensureNewEmail(normalizedEmail);
        ensurePassword(password);

        Applicant candidate = PersonFactory.createApplicant(
            name.trim(),
            normalizedEmail,
            (background == null || background.isBlank()) ? "General Candidate Profile" : background.trim(),
            12,
            toSkillObjects(skills, 0.78)
        );

        candidatesByEmail.put(normalizedEmail, candidate);
        UserAccount account = new UserAccount(normalizedEmail, hashPassword(password), UserRole.CANDIDATE, name.trim());
        accountsByEmail.put(normalizedEmail, account);
        persistAll();
        return account;
    }

    public synchronized UserAccount registerRecruiter(
            String recruiterName,
            String companyName,
            String email,
            String password,
            List<String> requiredSkills) {
        if (recruiterName == null || recruiterName.isBlank()) {
            throw new IllegalArgumentException("Recruiter name is required.");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name is required.");
        }
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            throw new IllegalArgumentException("At least one required skill is needed.");
        }

        String normalizedEmail = normalizeEmail(email);
        ensureNewEmail(normalizedEmail);
        ensurePassword(password);

        Recruiter recruiter = PersonFactory.createRecruiter(
            recruiterName.trim(),
            normalizedEmail,
            companyName.trim(),
            "Open Internship Role",
            12,
            toSkillObjects(requiredSkills, 0.82)
        );

        recruitersByEmail.put(normalizedEmail, recruiter);
        UserAccount account = new UserAccount(normalizedEmail, hashPassword(password), UserRole.RECRUITER, companyName.trim());
        accountsByEmail.put(normalizedEmail, account);
        persistAll();
        return account;
    }

    public synchronized List<Applicant> getAllApplicants() {
        return candidatesByEmail.values().stream()
            .map(this::copyApplicant)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public synchronized List<Recruiter> getAllRecruiters() {
        return recruitersByEmail.values().stream()
            .map(this::copyRecruiter)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public synchronized Optional<Applicant> getApplicantByEmail(String email) {
        Applicant applicant = candidatesByEmail.get(normalizeEmail(email));
        return applicant == null ? Optional.empty() : Optional.of(copyApplicant(applicant));
    }

    public synchronized Optional<Recruiter> getRecruiterByEmail(String email) {
        Recruiter recruiter = recruitersByEmail.get(normalizeEmail(email));
        return recruiter == null ? Optional.empty() : Optional.of(copyRecruiter(recruiter));
    }

    public synchronized void addCandidateSkills(String email, List<String> skills) {
        Applicant candidate = candidatesByEmail.get(normalizeEmail(email));
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate profile not found.");
        }
        toSkillObjects(skills, 0.80).forEach(candidate::addSkill);
        persistAll();
    }

    public synchronized void addRecruiterRequiredSkills(String email, List<String> skills) {
        Recruiter recruiter = recruitersByEmail.get(normalizeEmail(email));
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter profile not found.");
        }
        toSkillObjects(skills, 0.84).forEach(recruiter::addSkill);
        persistAll();
    }

    public synchronized void addApplicantProfile(Applicant applicant) {
        if (applicant == null) return;
        candidatesByEmail.put(normalizeEmail(applicant.getEmail()), copyApplicant(applicant));
        persistAll();
    }

    public synchronized void addRecruiterProfile(Recruiter recruiter) {
        if (recruiter == null) return;
        recruitersByEmail.put(normalizeEmail(recruiter.getEmail()), copyRecruiter(recruiter));
        persistAll();
    }

    private void initialize() {
        try {
            Files.createDirectories(dataDir);
            if (!Files.exists(usersFile) || !Files.exists(candidatesFile) || !Files.exists(recruitersFile)) {
                seedAndPersist();
                return;
            }
            loadAll();
            if (accountsByEmail.isEmpty() || candidatesByEmail.isEmpty() || recruitersByEmail.isEmpty()) {
                seedAndPersist();
            }
        } catch (Exception e) {
            seedAndPersist();
        }
    }

    private void seedAndPersist() {
        accountsByEmail.clear();
        candidatesByEmail.clear();
        recruitersByEmail.clear();

        for (Applicant applicant : seedService.seedApplicants()) {
            String email = normalizeEmail(applicant.getEmail());
            candidatesByEmail.put(email, copyApplicant(applicant));
            accountsByEmail.put(email, new UserAccount(email, hashPassword(DEFAULT_SEED_PASSWORD), UserRole.CANDIDATE, applicant.getName()));
        }

        for (Recruiter recruiter : seedService.seedRecruiters()) {
            String email = normalizeEmail(recruiter.getEmail());
            recruitersByEmail.put(email, copyRecruiter(recruiter));
            accountsByEmail.put(email, new UserAccount(email, hashPassword(DEFAULT_SEED_PASSWORD), UserRole.RECRUITER, recruiter.getCompany()));
        }

        persistAll();
    }

    private void loadAll() throws IOException {
        accountsByEmail.clear();
        candidatesByEmail.clear();
        recruitersByEmail.clear();

        List<String> userLines = Files.readAllLines(usersFile, StandardCharsets.UTF_8);
        for (int i = 1; i < userLines.size(); i++) {
            String line = userLines.get(i);
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            if (parts.length < 4) continue;
            String email = normalizeEmail(parts[0]);
            UserRole role = UserRole.valueOf(parts[2]);
            accountsByEmail.put(email, new UserAccount(email, parts[1], role, parts[3]));
        }

        List<String> candidateLines = Files.readAllLines(candidatesFile, StandardCharsets.UTF_8);
        for (int i = 1; i < candidateLines.size(); i++) {
            String line = candidateLines.get(i);
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            if (parts.length < 5) continue;
            String email = normalizeEmail(parts[0]);
            Applicant applicant = PersonFactory.createApplicant(
                parts[1],
                email,
                parts[2],
                Integer.parseInt(parts[3]),
                decodeSkills(parts[4])
            );
            candidatesByEmail.put(email, applicant);
        }

        List<String> recruiterLines = Files.readAllLines(recruitersFile, StandardCharsets.UTF_8);
        for (int i = 1; i < recruiterLines.size(); i++) {
            String line = recruiterLines.get(i);
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            if (parts.length < 6) continue;
            String email = normalizeEmail(parts[0]);
            Recruiter recruiter = PersonFactory.createRecruiter(
                parts[1],
                email,
                parts[2],
                parts[3],
                Integer.parseInt(parts[4]),
                decodeSkills(parts[5])
            );
            recruitersByEmail.put(email, recruiter);
        }
    }

    private void persistAll() {
        try {
            List<String> userLines = new ArrayList<>();
            userLines.add("email\tpasswordHash\trole\tdisplayName");
            for (UserAccount account : accountsByEmail.values()) {
                userLines.add(String.join("\t",
                    account.getEmail(),
                    account.getPasswordHash(),
                    account.getRole().name(),
                    account.getDisplayName()
                ));
            }
            Files.write(usersFile, userLines, StandardCharsets.UTF_8);

            List<String> candidateLines = new ArrayList<>();
            candidateLines.add("email\tname\tdesiredRole\tavailabilityWeeks\tskills");
            for (Applicant candidate : candidatesByEmail.values()) {
                candidateLines.add(String.join("\t",
                    candidate.getEmail(),
                    candidate.getName(),
                    candidate.getDesiredRole(),
                    String.valueOf(candidate.getAvailabilityWeeks()),
                    encodeSkills(candidate.getSkills())
                ));
            }
            Files.write(candidatesFile, candidateLines, StandardCharsets.UTF_8);

            List<String> recruiterLines = new ArrayList<>();
            recruiterLines.add("email\trecruiterName\tcompany\tinternshipTitle\tdurationWeeks\trequiredSkills");
            for (Recruiter recruiter : recruitersByEmail.values()) {
                recruiterLines.add(String.join("\t",
                    recruiter.getEmail(),
                    recruiter.getName(),
                    recruiter.getCompany(),
                    recruiter.getInternshipTitle(),
                    String.valueOf(recruiter.getDurationWeeks()),
                    encodeSkills(recruiter.getSkills())
                ));
            }
            Files.write(recruitersFile, recruiterLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to persist local database", e);
        }
    }

    private void ensureNewEmail(String email) {
        if (email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required.");
        }
        if (accountsByEmail.containsKey(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
    }

    private void ensurePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    private List<Skill> toSkillObjects(List<String> names, double defaultWeight) {
        if (names == null) return List.of();
        List<Skill> skills = new ArrayList<>();
        for (String rawName : names) {
            if (rawName == null || rawName.isBlank()) continue;
            skills.add(new Skill(rawName.trim(), defaultWeight));
        }
        return skills;
    }

    private String encodeSkills(List<Skill> skills) {
        return skills.stream()
            .map(skill -> skill.getName() + ":" + String.format(Locale.ROOT, "%.2f", skill.getWeight()))
            .collect(Collectors.joining("|"));
    }

    private List<Skill> decodeSkills(String encoded) {
        if (encoded == null || encoded.isBlank()) return List.of();
        List<Skill> skills = new ArrayList<>();
        for (String pair : encoded.split("\\|")) {
            if (pair.isBlank()) continue;
            String[] parts = pair.split(":", 2);
            if (parts.length < 2) continue;
            try {
                skills.add(new Skill(parts[0], Double.parseDouble(parts[1])));
            } catch (RuntimeException ignored) {
                // Ignore malformed entries.
            }
        }
        return skills;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashPassword(String password) {
        String raw = password == null ? "" : password;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    private Applicant copyApplicant(Applicant source) {
        return PersonFactory.createApplicant(
            source.getName(),
            source.getEmail(),
            source.getDesiredRole(),
            source.getAvailabilityWeeks(),
            source.getSkills()
        );
    }

    private Recruiter copyRecruiter(Recruiter source) {
        return PersonFactory.createRecruiter(
            source.getName(),
            source.getEmail(),
            source.getCompany(),
            source.getInternshipTitle(),
            source.getDurationWeeks(),
            source.getSkills()
        );
    }
}
