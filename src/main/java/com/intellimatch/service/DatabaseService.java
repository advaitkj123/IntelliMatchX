package com.intellimatch.service;

import com.intellimatch.factory.PersonFactory;
import com.intellimatch.model.Applicant;
import com.intellimatch.model.AnalyticsSnapshot;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final Path userStatsFile = dataDir.resolve("user_stats.tsv");
    private final Path shortlistsFile = dataDir.resolve("shortlists.tsv");

    private final Map<String, UserAccount> accountsByEmail = new LinkedHashMap<>();
    private final Map<String, Applicant> candidatesByEmail = new LinkedHashMap<>();
    private final Map<String, Recruiter> recruitersByEmail = new LinkedHashMap<>();
    private final Map<String, UserStat> userStatsByEmail = new LinkedHashMap<>();
    private final List<ShortlistEntry> shortlists = new ArrayList<>();

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
        userStatsByEmail.put(normalizedEmail, new UserStat(0, 0, 0L));
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
        userStatsByEmail.put(normalizedEmail, new UserStat(0, 0, 0L));
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
        String normalized = normalizeEmail(email);
        Applicant candidate = candidatesByEmail.get(normalized);
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate profile not found.");
        }
        toSkillObjects(skills, 0.80).forEach(candidate::addSkill);
        markActivity(normalized);
        persistAll();
    }

    public synchronized void addRecruiterRequiredSkills(String email, List<String> skills) {
        Recruiter recruiter = recruitersByEmail.get(normalizeEmail(email));
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter profile not found.");
        }
        toSkillObjects(skills, 0.84).forEach(recruiter::addSkill);
        markActivity(normalizeEmail(email));
        persistAll();
    }

    public synchronized void updateRecruiterPostingControls(
            String email,
            String internshipTitle,
            String roleLevel,
            String location,
            String stipend,
            String startDate,
            String workMode) {
        Recruiter recruiter = recruitersByEmail.get(normalizeEmail(email));
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter profile not found.");
        }
        if (internshipTitle != null && !internshipTitle.isBlank()) recruiter.setInternshipTitle(internshipTitle.trim());
        if (roleLevel != null && !roleLevel.isBlank()) recruiter.setRoleLevel(roleLevel.trim());
        if (location != null && !location.isBlank()) recruiter.setLocation(location.trim());
        if (stipend != null && !stipend.isBlank()) recruiter.setStipend(stipend.trim());
        if (startDate != null && !startDate.isBlank()) recruiter.setStartDate(startDate.trim());
        if (workMode != null && !workMode.isBlank()) recruiter.setWorkMode(workMode.trim());
        markActivity(normalizeEmail(email));
        persistAll();
    }

    public synchronized void recordLogin(String email) {
        String normalized = normalizeEmail(email);
        UserStat stat = userStatsByEmail.computeIfAbsent(normalized, e -> new UserStat(0, 0, 0L));
        stat.loginCount++;
        stat.lastLoginEpochMillis = System.currentTimeMillis();
        persistAll();
    }

    public synchronized void recordActivity(String email) {
        markActivity(normalizeEmail(email));
        persistAll();
    }

    public synchronized void shortlistCandidate(String recruiterEmail, String candidateEmail) {
        String recruiter = normalizeEmail(recruiterEmail);
        String candidate = normalizeEmail(candidateEmail);
        if (!recruitersByEmail.containsKey(recruiter)) {
            throw new IllegalArgumentException("Recruiter profile not found.");
        }
        if (!candidatesByEmail.containsKey(candidate)) {
            throw new IllegalArgumentException("Candidate profile not found.");
        }
        boolean exists = shortlists.stream().anyMatch(s ->
            s.recruiterEmail.equals(recruiter) && s.candidateEmail.equals(candidate));
        if (!exists) {
            shortlists.add(new ShortlistEntry(recruiter, candidate, System.currentTimeMillis()));
            markActivity(recruiter);
            persistAll();
        }
    }

    public synchronized AnalyticsSnapshot getAnalyticsSnapshot() {
        int signedUp = accountsByEmail.size();
        int active = (int) userStatsByEmail.values().stream()
            .filter(s -> s.loginCount > 0 || s.actionCount > 0)
            .count();
        Set<String> shortlistedCandidates = shortlists.stream()
            .map(s -> s.candidateEmail)
            .collect(Collectors.toSet());

        Map<String, Long> topCandidateSkills = aggregateTopSkillsFromApplicants(10);
        Map<String, Long> marketDemandSkills = aggregateTopSkillsFromRecruiters(10);
        Map<String, Long> workModeDemand = recruitersByEmail.values().stream()
            .collect(Collectors.groupingBy(
                recruiter -> recruiter.getWorkMode() == null || recruiter.getWorkMode().isBlank()
                    ? "Not specified"
                    : recruiter.getWorkMode(),
                LinkedHashMap::new,
                Collectors.counting()
            ));

        return new AnalyticsSnapshot(
            signedUp,
            active,
            shortlistedCandidates.size(),
            candidatesByEmail.size(),
            recruitersByEmail.size(),
            topCandidateSkills,
            marketDemandSkills,
            workModeDemand
        );
    }

    public synchronized void addApplicantProfile(Applicant applicant) {
        if (applicant == null) return;
        String email = normalizeEmail(applicant.getEmail());
        candidatesByEmail.put(email, copyApplicant(applicant));
        userStatsByEmail.putIfAbsent(email, new UserStat(0, 0, 0L));
        persistAll();
    }

    public synchronized void addRecruiterProfile(Recruiter recruiter) {
        if (recruiter == null) return;
        String email = normalizeEmail(recruiter.getEmail());
        recruitersByEmail.put(email, copyRecruiter(recruiter));
        userStatsByEmail.putIfAbsent(email, new UserStat(0, 0, 0L));
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
                return;
            }
            if (!Files.exists(userStatsFile) || !Files.exists(shortlistsFile)) {
                persistAll();
            }
        } catch (Exception e) {
            seedAndPersist();
        }
    }

    private void seedAndPersist() {
        accountsByEmail.clear();
        candidatesByEmail.clear();
        recruitersByEmail.clear();
        userStatsByEmail.clear();
        shortlists.clear();

        for (Applicant applicant : seedService.seedApplicants()) {
            String email = normalizeEmail(applicant.getEmail());
            candidatesByEmail.put(email, copyApplicant(applicant));
            accountsByEmail.put(email, new UserAccount(email, hashPassword(DEFAULT_SEED_PASSWORD), UserRole.CANDIDATE, applicant.getName()));
            userStatsByEmail.put(email, new UserStat(0, 0, 0L));
        }

        for (Recruiter recruiter : seedService.seedRecruiters()) {
            String email = normalizeEmail(recruiter.getEmail());
            recruitersByEmail.put(email, copyRecruiter(recruiter));
            accountsByEmail.put(email, new UserAccount(email, hashPassword(DEFAULT_SEED_PASSWORD), UserRole.RECRUITER, recruiter.getCompany()));
            userStatsByEmail.put(email, new UserStat(0, 0, 0L));
        }

        persistAll();
    }

    private void loadAll() throws IOException {
        accountsByEmail.clear();
        candidatesByEmail.clear();
        recruitersByEmail.clear();
        userStatsByEmail.clear();
        shortlists.clear();

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
            String roleLevel = parts.length > 6 ? parts[6] : "Intern";
            String location = parts.length > 7 ? parts[7] : "Not specified";
            String stipend = parts.length > 8 ? parts[8] : "Not specified";
            String startDate = parts.length > 9 ? parts[9] : "Flexible";
            String workMode = parts.length > 10 ? parts[10] : "Hybrid";
            Recruiter recruiter = PersonFactory.createRecruiter(
                parts[1],
                email,
                parts[2],
                parts[3],
                Integer.parseInt(parts[4]),
                roleLevel,
                location,
                stipend,
                startDate,
                workMode,
                decodeSkills(parts[5])
            );
            recruitersByEmail.put(email, recruiter);
        }

        if (Files.exists(userStatsFile)) {
            List<String> statLines = Files.readAllLines(userStatsFile, StandardCharsets.UTF_8);
            for (int i = 1; i < statLines.size(); i++) {
                String line = statLines.get(i);
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length < 4) continue;
                String email = normalizeEmail(parts[0]);
                userStatsByEmail.put(email, new UserStat(
                    parseIntSafe(parts[1], 0),
                    parseIntSafe(parts[2], 0),
                    parseLongSafe(parts[3], 0L)
                ));
            }
        }

        if (Files.exists(shortlistsFile)) {
            List<String> shortlistLines = Files.readAllLines(shortlistsFile, StandardCharsets.UTF_8);
            for (int i = 1; i < shortlistLines.size(); i++) {
                String line = shortlistLines.get(i);
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length < 3) continue;
                shortlists.add(new ShortlistEntry(
                    normalizeEmail(parts[0]),
                    normalizeEmail(parts[1]),
                    parseLongSafe(parts[2], 0L)
                ));
            }
        }

        for (String email : accountsByEmail.keySet()) {
            userStatsByEmail.putIfAbsent(email, new UserStat(0, 0, 0L));
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
            recruiterLines.add("email\trecruiterName\tcompany\tinternshipTitle\tdurationWeeks\trequiredSkills\troleLevel\tlocation\tstipend\tstartDate\tworkMode");
            for (Recruiter recruiter : recruitersByEmail.values()) {
                recruiterLines.add(String.join("\t",
                    recruiter.getEmail(),
                    recruiter.getName(),
                    recruiter.getCompany(),
                    recruiter.getInternshipTitle(),
                    String.valueOf(recruiter.getDurationWeeks()),
                    encodeSkills(recruiter.getSkills()),
                    safeValue(recruiter.getRoleLevel()),
                    safeValue(recruiter.getLocation()),
                    safeValue(recruiter.getStipend()),
                    safeValue(recruiter.getStartDate()),
                    safeValue(recruiter.getWorkMode())
                ));
            }
            Files.write(recruitersFile, recruiterLines, StandardCharsets.UTF_8);

            List<String> statLines = new ArrayList<>();
            statLines.add("email\tloginCount\tactionCount\tlastLoginEpochMillis");
            for (Map.Entry<String, UserStat> entry : userStatsByEmail.entrySet()) {
                UserStat stat = entry.getValue();
                statLines.add(String.join("\t",
                    entry.getKey(),
                    String.valueOf(stat.loginCount),
                    String.valueOf(stat.actionCount),
                    String.valueOf(stat.lastLoginEpochMillis)
                ));
            }
            Files.write(userStatsFile, statLines, StandardCharsets.UTF_8);

            List<String> shortlistLines = new ArrayList<>();
            shortlistLines.add("recruiterEmail\tcandidateEmail\ttimestamp");
            for (ShortlistEntry entry : shortlists) {
                shortlistLines.add(String.join("\t",
                    entry.recruiterEmail,
                    entry.candidateEmail,
                    String.valueOf(entry.timestamp)
                ));
            }
            Files.write(shortlistsFile, shortlistLines, StandardCharsets.UTF_8);
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

    private void markActivity(String email) {
        UserStat stat = userStatsByEmail.computeIfAbsent(email, e -> new UserStat(0, 0, 0L));
        stat.actionCount++;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private int parseIntSafe(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private long parseLongSafe(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private Map<String, Long> aggregateTopSkillsFromApplicants(int limit) {
        return candidatesByEmail.values().stream()
            .flatMap(applicant -> applicant.getSkills().stream())
            .collect(Collectors.groupingBy(Skill::getName, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
            ));
    }

    private Map<String, Long> aggregateTopSkillsFromRecruiters(int limit) {
        return recruitersByEmail.values().stream()
            .flatMap(recruiter -> recruiter.getSkills().stream())
            .collect(Collectors.groupingBy(Skill::getName, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
            ));
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
            source.getRoleLevel(),
            source.getLocation(),
            source.getStipend(),
            source.getStartDate(),
            source.getWorkMode(),
            source.getSkills()
        );
    }

    private static final class UserStat {
        private int loginCount;
        private int actionCount;
        private long lastLoginEpochMillis;

        private UserStat(int loginCount, int actionCount, long lastLoginEpochMillis) {
            this.loginCount = loginCount;
            this.actionCount = actionCount;
            this.lastLoginEpochMillis = lastLoginEpochMillis;
        }
    }

    private static final class ShortlistEntry {
        private final String recruiterEmail;
        private final String candidateEmail;
        private final long timestamp;

        private ShortlistEntry(String recruiterEmail, String candidateEmail, long timestamp) {
            this.recruiterEmail = recruiterEmail;
            this.candidateEmail = candidateEmail;
            this.timestamp = timestamp;
        }
    }
}
