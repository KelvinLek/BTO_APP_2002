package service;

import entity.User;

/**
 * Defines the contract for user authentication services.
 */
public interface IAuthService {

    /**
     * Authenticates a user based on their ID and password.
     *
     * @param userId   The user's ID, used as the login identifier.
     * @param password The password provided by the user.
     * @return The authenticated User object if the credentials are valid,
     * returns null if authentication fails.
     */
    User login(String userId, String password);
}