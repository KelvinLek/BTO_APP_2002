package service;

import entity.User; // Assuming User entity exists in the entity package

/**
 * Defines the contract for user authentication services.
 * Implementations of this interface are responsible for validating user credentials.
 */
public interface IAuthService {

    /**
     * Authenticates a user based on their NRIC (User ID) and password.
     * Implementations should check against stored user data.
     * As per the brief, the NRIC format starts with S or T, followed by 7 digits,
     * and ends with a letter. The default password is "password".
     *
     * @param nric     The user's NRIC, used as the login ID.
     * @param password The password provided by the user.
     * @return The authenticated User object (could be Applicant, HdbOfficer, or HdbManager)
     * if the credentials are valid and the NRIC format is correct.
     * Returns null if authentication fails (user not found, incorrect password, or invalid NRIC format).
     * Implementations should handle potential errors gracefully (e.g., return null rather than throwing exceptions for standard auth failures).
     */
    User login(String nric, String password);
}