package service;

import entity.HdbOfficer;
import entity.Project;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Defines the contract for services specific to HDB Officer registration actions.
 * Other officer actions (booking flats, replying enquiries) are in separate interfaces.
 */
public interface IHdbOfficerService {

    /**
     * Registers an HDB Officer's interest in being assigned to a specific project.
     * This typically sets a status to "Pending" for manager approval.
     * Checks eligibility:
     * - Officer cannot have applied for this project as an Applicant.
     * - Officer cannot be handling another project during the same application period.
     *
     * @param officer   The HdbOfficer entity representing the officer registering.
     * @param project   The Project the officer wants to register for.
     * @return true if the registration request was successfully logged, false otherwise.
     * @throws NoSuchElementException   if the officer or project is not found.
     * @throws IllegalStateException    if the officer is ineligible (applied as applicant, or has overlapping duty).
     * @throws Exception                for underlying persistence errors.
     */
    boolean registerForProject(HdbOfficer officer, Project project) throws NoSuchElementException, IllegalStateException, Exception;
    // Renamed method from applyForProject for clarity based on brief

    /**
     * Retrieves the status of an HDB Officer's registration request for a project.
     *
     * @param officer The HdbOfficer whose status is being checked.
     * @param project The Project the officer registered for.
     * @return String representing the status (e.g., "Pending Registration", "Approved", "Rejected", "Not Registered").
     * @throws NoSuchElementException if officer or project not found.
     */
    String viewRegistrationStatus(HdbOfficer officer, Project project) throws NoSuchElementException;

}