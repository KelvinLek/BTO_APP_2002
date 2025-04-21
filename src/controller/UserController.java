package controller;

import entity.User;
import service.*; // Import required service interfaces/classes
import java.util.NoSuchElementException;

/**
 * Base controller handling common user actions like login and password change.
 * Requires a UserService instance for its operations.
 */
public class UserController { // Removed 'abstract' as it might be instantiated directly for login

    private final IAuthService authService;
    private final IPasswordService passwordService;

    /**
     * Constructor for UserController.
     * @param authService Service for authentication.
     * @param passwordService Service for password management.
     */
    public UserController(IAuthService authService, IPasswordService passwordService) {
        this.authService = authService;
        this.passwordService = passwordService;
    }

    /**
     * Handles the user login process.
     * Takes NRIC and password, calls the authentication service.
     *
     * @param nric     The NRIC entered by the user.
     * @param password The password entered by the user.
     * @return The authenticated User object if successful, null otherwise.
     */
    public User handleLogin(String nric, String password) {
        try {
            User user = authService.login(nric, password);
            if (user != null) {
                System.out.println("Login successful. Welcome " + user.getName() + "!");
                // Check for default password and prompt change if needed
                if ("password".equals(password)) {
                    System.out.println("WARNING: You are using the default password. Please change it immediately.");
                    // Optionally force password change here or let the role-specific controller handle it.
                }
                return user;
            } else {
                System.out.println("Login failed. Please check your NRIC and password.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles the password change process for the currently logged-in user.
     *
     * @param currentUser The currently logged-in User object.
     * @param newPassword The new password entered by the user.
     * @return true if the password change was successful, false otherwise.
     */
    public boolean handleChangePassword(User currentUser, String newPassword) {
        if (currentUser == null) {
            System.out.println("Error: No user logged in to change password.");
            return false;
        }
        // Basic validation (can add more rules)
        if (newPassword == null || newPassword.isEmpty() || newPassword.length() < 6) {
            System.out.println("Error: New password must be at least 6 characters long.");
            return false;
        }


        try {
            boolean success = passwordService.changePassword(currentUser, newPassword);
            if (success) {
                System.out.println("Password changed successfully.");
                return true;
            } else {
                // Specific error messages should be printed by the service layer
                // System.out.println("Password change failed. Please try again."); // Generic message if service doesn't print
                return false;
            }
        } catch (NoSuchElementException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during password change: " + e.getMessage());
            return false;
        }
    }
}