package service;

import entity.Applicant; // Assuming specific Applicant entity exists and extends User
import entity.Application;
import entity.Project;
import pub_enums.FlatType; // Assuming enum exists for flat types
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Defines the contract for services specific to BTO Applicants.
 * Covers viewing projects, applying, viewing status, and withdrawing.
 */
public interface IApplicantService {

    /**
     * Retrieves a list of BTO projects visible and eligible for the given applicant.
     * Checks project visibility, application period, and applicant's eligibility
     * (age, marital status) against available flat types.
     *
     * @param applicant The Applicant object making the request.
     * @return A List of Project objects the applicant can view and potentially apply for.
     * Returns an empty list if none are available or eligible.
     */
    List<Project> viewAvailableProjects(Applicant applicant);

    /**
     * Submits a BTO application for a given applicant and project.
     * Performs eligibility checks based on the assignment brief rules:
     * - Singles >= 35 only for 2-Room.
     * - Married >= 21 for any flat type.
     * - Cannot apply for multiple projects simultaneously (check existing non-final applications).
     * - Project must be open for applications.
     *
     * @param applicant The Applicant submitting the application.
     * @param project   The Project being applied for.
     * @param flatType  The specific FlatType the applicant is applying for.
     * @return true if the application was submitted successfully (status set to Pending), false otherwise.
     * @throws IllegalArgumentException if eligibility rules are violated, project invalid, or applicant already has an active application.
     * @throws NoSuchElementException   if applicant or project not found.
     * @throws Exception                for underlying persistence errors.
     */
    boolean applyForProject(Applicant applicant, Project project, FlatType flatType) throws IllegalArgumentException, NoSuchElementException, Exception;

    /**
     * Retrieves the current status of the applicant's BTO application(s).
     * An applicant should typically only have one non-final application.
     *
     * @param applicant The Applicant whose status is being checked.
     * @return The Application object representing the applicant's current application,
     * or null if the applicant has no active or recent application.
     */
    Application getApplicationStatus(Applicant applicant); // Simplified return type

    /**
     * Initiates a withdrawal request for the applicant's current BTO application.
     * Sets the application status to indicate withdrawal request (e.g., "WITHDRAW_PENDING").
     * This request needs approval from an HDB Manager via IWithdrawalService.
     * Withdrawal can be requested before or after booking as per brief.
     *
     * @param application The Application object to be withdrawn.
     * @return true if the withdrawal request was successfully logged (status updated), false otherwise.
     * @throws NoSuchElementException if the application is not found or not in a state allowing withdrawal request.
     * @throws Exception              for underlying persistence errors.
     */
    boolean requestWithdrawal(Application application) throws NoSuchElementException, Exception;
}