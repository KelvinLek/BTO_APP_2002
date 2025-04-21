package service;

import entity.User;
import java.util.NoSuchElementException;

/**
 * Defines the contract for services related to changing user passwords.
 * Separated according to ISP, focusing solely on password modification.
 */
public interface IPasswordService {

    /**
     * Changes the password for the specified user.
     * Implementations should find the user and update their password securely.
     *
     * @param user        The User object whose password needs changing.
     * @param newPassword The new password to set.
     * @return true if the password was changed successfully, false otherwise (e.g., validation failed).
     * @throws NoSuchElementException if the user object provided is invalid or not found in the data store.
     * @throws Exception              for underlying persistence errors.
     */
    boolean changePassword(User user, String newPassword) throws NoSuchElementException, Exception; // Changed signature to take User object

    // Note: Original IUserService also had changePassword. This separate interface
    // adheres more strictly to ISP if only password changing is needed.
    // The UserService class can implement both. Consider if the User parameter
    // is better than userId depending on how user context is managed.
}