package com.eventify.util;

import com.eventify.model.User;

/**
 * Manages the currently authenticated user session across JavaFX controllers.
 */
public class SessionManager {

    private static volatile User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isOrganizer() {
        return currentUser != null
                && "admin".equalsIgnoreCase(currentUser.getUsername())
                && currentUser.canManageSystem();
    }

    public static void clearSession() {
        currentUser = null;
    }
}
