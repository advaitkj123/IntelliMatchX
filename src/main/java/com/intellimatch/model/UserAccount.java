package com.intellimatch.model;

public class UserAccount {

    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final String displayName;

    public UserAccount(String email, String passwordHash, UserRole role, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.displayName = displayName;
    }

    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public String getDisplayName() { return displayName; }
}
