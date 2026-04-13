package com.intellimatch.service;

import com.intellimatch.model.UserAccount;

import java.util.Optional;

public final class SessionManager {

    private static UserAccount currentUser;

    private SessionManager() {}

    public static void setCurrentUser(UserAccount account) {
        currentUser = account;
    }

    public static Optional<UserAccount> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static void clear() {
        currentUser = null;
    }
}
