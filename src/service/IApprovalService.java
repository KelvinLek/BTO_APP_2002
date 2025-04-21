package service;

import entity.Application;
import entity.HdbManager; // Approving user is likely a manager
import entity.HdbOfficer; // For officer registration approval
import entity.Project; // For officer registration approval context
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Defines the contract for approval/rejection services, typically performed by HDB Managers.
 * Separated from IManagerService based on ISP.
 */
public interface IApprovalService {

    /**
     * Approves or rejects an applicant's BTO application.
     * If approving, checks for flat availability for the requested type.
     * Updates application status (Successful/Unsuccessful) and project flat counts accordingly.
     *
     * @param application The Application to be processed.
     * @param manager     The HdbManager performing the action.
     * @param approve     True to approve, false to reject.
     * @return true if the application status was successfully updated, false otherwise.
     * @throws NoSuchElementException if the application or associated project is not found.
     * @throws IllegalStateException  if approving but no units are available, or application not in correct state.
     * @throws Exception              for underlying persistence errors.
     */
    boolean processApplicationApproval(Application application, HdbManager manager, boolean approve) throws NoSuchElementException, IllegalStateException, Exception;
// Renamed from approveApplication/rejectApplication for clarity