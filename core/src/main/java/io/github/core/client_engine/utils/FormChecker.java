package io.github.core.client_engine.utils;

import java.util.regex.Pattern;

/**
 * <h1>Form Checker</h1>
 * <p>A utility class that help with checking if a given email, password, username... is valid.</p>
 */
public final class FormChecker {


    // Regex pour email (Java standard)
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }


    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        return hasLetter && hasDigit;
    }


    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) return false;
        if (username.length() < 3 || username.length() > 20) return false;

        return username.matches("^[a-zA-Z0-9_]+$");
    }


    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }


}
