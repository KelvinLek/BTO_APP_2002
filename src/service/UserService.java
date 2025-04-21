package service;

import entity.User;
import pub_enums.Role; // Assuming Role enum exists
import java.util.Date; // Assuming User has Date of Birth
import java.util.NoSuchElementException;
// Import necessary repository interfaces if interacting with data storage
// import repository.IUserRepo;

/**
 * Abstract base class for user-related services, providing common
 * authentication and password management functionalities.
 */
public abstract class UserService implements IAuthService, IPasswordService {

    // Assume repository injection if data persistence is implemented
    // private final IUserRepo userRepo;
    //
    // public UserService(IUserRepo userRepo) {
    //     this.userRepo = userRepo;
    // }

    /**
     * Authenticates a user based on their NRIC (User ID) and password.
     * Checks against stored user data and validates NRIC format.
     * Default password is "password".
     *
     * @param nric     The user's NRIC.
     * @param password The user's password.
     * @return The authenticated User object (Applicant, HdbOfficer, HdbManager) or null if authentication fails.
     */
    @Override
    public User login(String nric, String password) {
        // 1. Validate NRIC format
        if (nric == null || !nric.matches("^[STst]\\d{7}[A-Za-z]$")) {
            System.out.println("Invalid NRIC format.");
            return null;
        }

        // 2. Find user by NRIC (replace with actual repository call)
        // User user = userRepo.findById(nric); // Example repo call
        User user = findUserByNricPlaceholder(nric); // Placeholder call

        // 3. Check if user exists and password matches
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login successful for " + nric);
            return user;
        } else if (user != null && password.equals("password") && user.getPassword() == null /* Or check if it's default */) {
            // Handle default password scenario if needed - maybe force change?
            System.out.println("Login successful with default password for " + nric);
            return user;
        }

        System.out.println("Login failed for " + nric);
        return null;
    }

    /**
     * Changes the password for the specified user.
     *
     * @param user        The User object whose password needs changing.
     * @param newPassword The new password to set.
     * @return true if the password was changed successfully, false otherwise.
     * @throws NoSuchElementException if the user is invalid.
     * @throws Exception              for other errors (e.g., saving).
     */
    @Override
    public boolean changePassword(User user, String newPassword) throws NoSuchElementException, Exception {
        if (user == null || user.getId() == null) {
            throw new NoSuchElementException("Invalid user object provided for password change.");
        }

        // 1. Find user to ensure they exist (optional, as User object is passed in)
        // User existingUser = userRepo.findById(user.getId()); // Example repo call
        User existingUser = findUserByNricPlaceholder(user.getId()); // Placeholder call
        if (existingUser == null) {
            throw new NoSuchElementException("User with ID " + user.getId() + " not found.");
        }

        // 2. Validate new password (add complexity rules if needed)
        if (newPassword == null || newPassword.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        if (newPassword.equals(existingUser.getPassword())) {
            System.out.println("New password cannot be the same as the old password.");
            return false;
        }


        // 3. Update password in the user object
        existingUser.setPassword(newPassword); // Update the found user object

        // 4. Save updated user data (replace with actual repository call)
        try {
            // userRepo.save(existingUser); // Example repo call
            saveUserPlaceholder(existingUser); // Placeholder call
            System.out.println("Password changed successfully for user " + existingUser.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error saving password update for user " + existingUser.getId() + ": " + e.getMessage());
            // Optionally re-throw a more specific application exception
            throw new Exception("Failed to save password update.", e);
        }
    }


    //TODO REMOVE PLACEHOLDERS
    // --- Placeholder methods for repository interaction ---
    // Replace these with actual calls to your repository implementation

    private User findUserByNricPlaceholder(String nric) {
        // Simulate finding a user. Replace with actual DB/file lookup.
        // This is highly simplified for demonstration.
        if ("S1234567A".equalsIgnoreCase(nric)) {
            // Use dob=null for simplicity, replace with actual Date
            return new entity.Applicant("Alice Tan", nric, null, pub_enums.MaritalStatus.SINGLE, "password", Role.APPLICANT, null, null);
        } else if ("S7654321B".equalsIgnoreCase(nric)) {
            // Use dob=null for simplicity, replace with actual Date
            // HdbOfficer extends Applicant in the provided entity structure
            return new entity.HdbOfficer("Bob Lim", nric, null, pub_enums.MaritalStatus.MARRIED, "password", Role.HDBOFFICER, null, null, null);
        } else if ("T1111111C".equalsIgnoreCase(nric)) {
            // Use dob=null for simplicity, replace with actual Date
            return new entity.HdbManager("Charlie Lee", nric, null, pub_enums.MaritalStatus.MARRIED, "password", Role.HDBMANAGER, null);
        }
        return null;
    }

    private void saveUserPlaceholder(User user) throws Exception {
        // Simulate saving a user. Replace with actual DB/file write.
        System.out.println("Placeholder: Saving user data for " + user.getId());
        // In a real scenario, update the user record in your persistence layer.
    }
}