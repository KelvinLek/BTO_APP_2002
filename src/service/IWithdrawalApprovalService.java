package service;

import entity.Application;
import entity.HdbManager; // Approving user is likely a manager
import java.util.NoSuchElementException;

/**
 * Defines the contract for managing the approval/rejection of BTO application
 * withdrawal requests, typically performed by an HDB Manager.
 */
public interface IWithdrawalApprovalService {

    /**
     * Approves an applicant's request to withdraw their application.
     * Updates the application status (e.g., "Withdraw-Approved").
     * Should potentially handle returning allocated resources (e.g., incrementing flat count if booking was made).
     *
     * @param application The Application whose withdrawal request is being approved (should be in status "WITHDRAW_PENDING" or similar).
     * @param manager     The HdbManager performing the approval.
     * @return true if the approval was successful, false otherwise.
     * @throws NoSuchElementException if the application is not found or not in the correct state.
     * @throws Exception              for underlying persistence errors.
     */
    boolean approveWithdrawal(Application application, HdbManager manager) throws NoSuchElementException, Exception;

    /**
     * Rejects an applicant's request to withdraw their application.
     * Updates the application status (e.g., back to its previous state like "Successful" or "Booked", or a specific "Withdraw-Rejected" status).
     *
     * @param application The Application whose withdrawal request is being rejected.
     * @param manager     The HdbManager performing the rejection.
     * @return true if the rejection was successful, false otherwise.
     * @throws NoSuchElementException if the application is not found or not in the correct state.
     * @throws Exception              for underlying persistence errors.
     */
    boolean rejectWithdrawal(Application application, HdbManager manager) throws NoSuchElementException, Exception;
}