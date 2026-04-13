package com.intellimatch.service;

import com.intellimatch.model.UserAccount;
import com.intellimatch.model.UserRole;

import java.util.List;
import java.util.Optional;

public class AuthService {

    private final DatabaseService databaseService = DatabaseService.getInstance();

    public Optional<UserAccount> loginCandidate(String email, String password) {
        return databaseService.authenticate(email, password, UserRole.CANDIDATE);
    }

    public Optional<UserAccount> loginRecruiter(String email, String password) {
        return databaseService.authenticate(email, password, UserRole.RECRUITER);
    }

    public UserAccount signUpCandidate(
            String name,
            String email,
            String password,
            String background,
            List<String> skills) {
        return databaseService.registerCandidate(name, email, password, background, skills);
    }

    public UserAccount signUpRecruiter(
            String recruiterName,
            String companyName,
            String email,
            String password,
            List<String> requiredSkills) {
        return databaseService.registerRecruiter(recruiterName, companyName, email, password, requiredSkills);
    }
}
