package com.banking.security;

import com.banking.model.User;
import com.banking.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Manages the current authenticated user session in the desktop application.
 * Thread-safe singleton with role-aware authorization helpers.
 */
public class UserSession {

    private static volatile UserSession currentSession;

    private final User user;
    private final String sessionToken;
    private final LocalDateTime loginTime;

    private UserSession(User user) {
        this.user = user;
        this.sessionToken = UUID.randomUUID().toString();
        this.loginTime = LocalDateTime.now();
    }

    /**
     * Creates a new authenticated session for the specified user.
     */
    public static synchronized UserSession startSession(User user) {
        currentSession = new UserSession(user);
        return currentSession;
    }

    /**
     * Retrieves the active user session or null if no user is currently authenticated.
     */
    public static synchronized UserSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Terminates and cleans up the active session.
     */
    public static synchronized void clearSession() {
        currentSession = null;
    }

    /**
     * Checks if there is an active logged-in user.
     */
    public static synchronized boolean isAuthenticated() {
        return currentSession != null && currentSession.getUser() != null;
    }

    public User getUser() {
        return user;
    }

    public long getUserId() {
        return user != null ? user.getUserId() : -1;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : "";
    }

    public String getFullName() {
        return user != null ? user.getFullName() : "";
    }

    public UserRole getRole() {
        return user != null ? user.getRole() : null;
    }

    public boolean isAdmin() {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
