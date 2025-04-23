package service;

import entity.User;
import java.util.NoSuchElementException;

/**
 * Defines the contract for password management services.
 */
public interface IPasswordService {

    /**
     * Changes the password for a specific user.
     *
     * @param user The user whose password needs to be changed.
     * @param newPassword The new password to set.
     * @return true if the password was changed successfully, false otherwise.
     * @throws NoSuchElementException if the user is not found.
     */
    boolean changePassword(User user, String newPassword) throws NoSuchElementException;
}