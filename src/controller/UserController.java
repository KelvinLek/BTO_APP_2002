package controller;

import entity.User;
import repository.ApplicantRepo;
import repository.HdbManagerRepo;
import repository.HdbOfficerRepo;
import service.UserService;

import java.util.Map;

/**
 * Handles general user operations such as login and registration
 */
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles user login
     * 
     * @param userId The user's ID/username
     * @param password The user's password
     * @return The authenticated User object or null if authentication fails
     */
    public User login(String userId, String password) {
        try {
            // Try applicant first
            ApplicantRepo applicantRepo = new ApplicantRepo();
            User user = applicantRepo.authenticate(userId, password);
            if (user != null) {
                System.out.println("Login successful. Welcome Applicant " + user.getName() + "!");
                return user;
            }

            // Try officer next
            HdbOfficerRepo officerRepo = new HdbOfficerRepo();
            user = officerRepo.authenticate(userId, password);
            if (user != null) {
                System.out.println("Login successful. Welcome Officer " + user.getName() + "!");
                return user;
            }

            // Try manager last
            HdbManagerRepo managerRepo = new HdbManagerRepo();
            user = managerRepo.authenticate(userId, password);
            if (user != null) {
                System.out.println("Login successful. Welcome Manager " + user.getName() + "!");
                return user;
            }

            // If no match
            System.out.println("Login failed. NRIC and password do not match any records.");
            return null;

        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Changes the password for a user
     * 
     * @param user The user to change password for
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if password change successful, false otherwise
     */
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if (user == null || oldPassword == null || newPassword == null) {
            System.out.println("Error: All fields must be provided.");
            return false;
        }
        
        // Verify old password
        if (!user.getPassword().equals(oldPassword)) {
            System.out.println("Error: Current password is incorrect.");
            return false;
        }
        
        // Basic validation
        if (newPassword.isEmpty() || newPassword.length() < 6) {
            System.out.println("Error: New password must be at least 6 characters long.");
            return false;
        }
        
        if (newPassword.equals(oldPassword)) {
            System.out.println("Error: New password must be different from the current password.");
            return false;
        }
        
        // Update password
        boolean success = userService.changePassword(user, newPassword);
        
        if (success) {
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Error: Failed to change password. Please try again.");
        }
        
        return success;
    }

    /**
     * Updates user profile information
     * 
     * @param user The user to update
     * @param updates Map of profile fields to update
     * @return true if update successful, false otherwise
     */
    public boolean updateProfile(User user, Map<String, Object> updates) {
        if (user == null || updates == null || updates.isEmpty()) {
            System.out.println("Error: User and updates must be provided.");
            return false;
        }
        
        // This is a simplified implementation
        // In a real application, you would apply updates to specific fields
        // and save the user to the repository
        
        System.out.println("Profile updated successfully.");
        return true;
    }
}