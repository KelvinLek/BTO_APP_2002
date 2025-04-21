package service;

import entity.Applicant;
import entity.Project; // Needed for context
import pub_enums.FlatType;

import java.util.NoSuchElementException;

/**
 * Defines the contract for checking applicant eligibility for BTO projects/flats.
 * This logic might be better embedded within IApplicantService.applyForProject,
 * but defined separately as per the BTO_App_2 structure.
 */
public interface IEligibilityCheck {

    /**
     * Checks if a given applicant is eligible to apply for a specific flat type
     * based on the rules in the assignment brief (age, marital status).
     *
     * @param applicant The Applicant whose eligibility is being checked.
     * @param flatType  The FlatType being considered.
     * @return true if the applicant is eligible for the flat type, false otherwise.
     * @throws NoSuchElementException if applicant data is missing or invalid.
     */
    boolean checkEligibility(Applicant applicant, FlatType flatType) throws NoSuchElementException;

    /**
     * Checks if an applicant is eligible to apply for a specific project,
     * considering their eligibility for *any* flat type offered by the project.
     * Also checks if the applicant already has an active application.
     *
     * @param applicant The Applicant.
     * @param project   The Project.
     * @return true if eligible to apply for the project, false otherwise.
     * @throws NoSuchElementException if applicant or project data is invalid.
     */
    boolean checkProjectApplicationEligibility(Applicant applicant, Project project) throws NoSuchElementException;

}