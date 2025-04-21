package service;

import entity.HdbManager;
import entity.HdbOfficer;
import entity.Project;
import java.util.Map; // For passing updated details
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Defines the contract for HDB Manager specific services related to project management (CRUD).
 * Approval services are typically separated (IApprovalService, IWithdrawalService).
 */
public interface IManagerService {

    /**
     * Creates a new BTO project listing.
     * Assigns the creating manager to the project.
     * Checks manager eligibility (cannot handle overlapping projects).
     *
     * @param manager The HdbManager creating the project.
     * @param projectDetails A Map containing the details for the new project
     * (e.g., name, neighbourhood, flat counts, dates, slots).
     * @return The newly created Project object.
     * @throws IllegalArgumentException if project details are invalid or the manager has an overlapping assignment.
     * @throws Exception                for underlying persistence errors.
     */
    Project createProject(HdbManager manager, Map<String, Object> projectDetails) throws IllegalArgumentException, Exception;

    /**
     * Edits the details of an existing BTO project.
     * Only the manager in charge should be allowed to edit (permission check needed).
     *
     * @param projectId      The ID of the project to edit.
     * @param updatedDetails A Map containing the fields to update and their new values.
     * @param editor         The HdbManager attempting the edit (for permission check).
     * @return true if the project was successfully updated, false otherwise.
     * @throws NoSuchElementException   if the project is not found.
     * @throws SecurityException        if the editor is not the manager in charge.
     * @throws IllegalArgumentException if updated details are invalid (e.g., negative units).
     * @throws Exception                for underlying persistence errors.
     */
    boolean editProject(String projectId, Map<String, Object> updatedDetails, HdbManager editor) throws NoSuchElementException, SecurityException, IllegalArgumentException, Exception;

    /**
     * Deletes an existing BTO project listing.
     * Only the manager in charge should be allowed to delete (permission check needed).
     * Consider implications (e.g., cannot delete if applications exist?). Brief doesn't specify.
     *
     * @param projectId The ID of the project to delete.
     * @param deleter   The HdbManager attempting the deletion.
     * @return true if deletion was successful, false otherwise.
     * @throws NoSuchElementException if the project is not found.
     * @throws SecurityException      if the deleter is not the manager in charge.
     * @throws IllegalStateException  if deletion is not allowed (e.g., active applications - needs clarification).
     * @throws Exception              for underlying persistence errors.
     */
    boolean deleteProject(String projectId, HdbManager deleter) throws NoSuchElementException, SecurityException, IllegalStateException, Exception;

    /**
     * Toggles the visibility of a project (on/off for applicants).
     * Only the manager in charge should be allowed to toggle (permission check needed).
     *
     * @param projectId The ID of the project.
     * @param isVisible The desired visibility state (true = on, false = off).
     * @param toggler   The HdbManager performing the action.
     * @return true if visibility was successfully updated, false otherwise.
     * @throws NoSuchElementException if the project is not found.
     * @throws SecurityException      if the toggler is not the manager in charge.
     * @throws Exception              for underlying persistence errors.
     */
    boolean toggleProjectVisibility(String projectId, boolean isVisible, HdbManager toggler) throws NoSuchElementException, SecurityException, Exception;

    /**
     * Approves or rejects an HDB Officer's registration request for a project.
     * If approving, checks eligibility (e.g., no overlaps, slots available).
     * Updates the officer's status for the project and the project's available slots.
     *
     * @param officer   The HdbOfficer whose registration is being processed.
     * @param project   The Project the officer registered for.
     * @param manager   The HdbManager performing the action (should be in charge of the project).
     * @param approve   True to approve, false to reject.
     * @return true if the registration status was successfully updated, false otherwise.
     * @throws NoSuchElementException   if officer or project not found.
     * @throws SecurityException        if the manager is not in charge of the project.
     * @throws IllegalStateException    if approval conditions (e.g., slots, overlap) are not met.
     * @throws Exception                for underlying persistence errors.
     */
    boolean processOfficerRegistration(HdbOfficer officer, Project project, HdbManager manager, boolean approve) throws NoSuchElementException, SecurityException, IllegalStateException, Exception;
    // Added method for Officer approval based on brief requirements
}