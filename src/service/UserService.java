package service;

import entity.User;
import pub_enums.Role;
import repository.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Base class for user-related services, providing common
 * authentication and password management functionalities.
 */
public class UserService implements IAuthService, IPasswordService {

    private ApplicantRepo applicantRepo;
    private HdbManagerRepo managerRepo;
    private HdbOfficerRepo officerRepo;

    public UserService() {
        // Default constructor for subclasses
    }

    public UserService(ApplicantRepo applicantRepo, HdbManagerRepo managerRepo, HdbOfficerRepo officerRepo) {
        this.applicantRepo = applicantRepo;
        this.managerRepo = managerRepo;
        this.officerRepo = officerRepo;
    }

    /**
     * Authenticates a user based on their ID and password.
     *
     * @param userId   The user's ID.
     * @param password The user's password.
     * @return The authenticated User object or null if authentication fails.
     */
    @Override
    public User login(String userId, String password) {
        if (userId == null || password == null) {
            return null;
        }

        // Try to find the user in all repositories
        User user = null;
        
        if (applicantRepo != null) {
            user = applicantRepo.findById(userId).orElse(null);
        }
        
        if (user == null && managerRepo != null) {
            user = managerRepo.findById(userId).orElse(null);
        }
        
        if (user == null && officerRepo != null) {
            user = officerRepo.findById(userId).orElse(null);
        }

        // Check password match
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }

    /**
     * Changes the password for the specified user.
     *
     * @param user        The User object whose password needs changing.
     * @param newPassword The new password to set.
     * @return true if the password was changed successfully, false otherwise.
     */
    @Override
    public boolean changePassword(User user, String newPassword) throws NoSuchElementException {
        if (user == null || user.getId() == null || newPassword == null) {
            throw new NoSuchElementException("Invalid user or password provided.");
        }

        // Validate new password
        if (newPassword.isEmpty() || newPassword.length() < 6) {
            return false;
        }

        // Update password
        user.setPassword(newPassword);

        // Save the updated user
        boolean result = false;
        
        try {
            if (user.getRole() == Role.APPLICANT && applicantRepo != null) {
                applicantRepo.update((entity.Applicant) user);
                result = true;
            } else if (user.getRole() == Role.HDBMANAGER && managerRepo != null) {
                managerRepo.update((entity.HdbManager) user);
                result = true;
            } else if (user.getRole() == Role.HDBOFFICER && officerRepo != null) {
                officerRepo.update((entity.HdbOfficer) user);
                result = true;
            }
        } catch (Exception e) {
            System.err.println("Error saving password update: " + e.getMessage());
            return false;
        }

        return result;
    }

    /**
     * Retrieves all users from all repositories.
     *
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        
        if (applicantRepo != null) {
            allUsers.addAll(applicantRepo.findAll());
        }
        
        if (managerRepo != null) {
            allUsers.addAll(managerRepo.findAll());
        }
        
        if (officerRepo != null) {
            allUsers.addAll(officerRepo.findAll());
        }
        
        return allUsers;
    }
}