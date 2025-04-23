package service;

import entity.Applicant;
import entity.User;
import entity.Project;
import pub_enums.FlatType;

import java.util.NoSuchElementException;

/**
 * Defines the contract for checking applicant eligibility for BTO projects/flats.
 */
public interface IEligibilityCheck {

    /**
     * Checks if a given user is eligible to apply for a specific flat type
     * based on the rules (age, marital status).
     *
     * @param user The User (typically Applicant) whose eligibility is being checked.
     * @param flatType The FlatType being considered.
     * @return true if the user is eligible for the flat type, false otherwise.
     * @throws NoSuchElementException if user data is missing or invalid.
     */
    boolean checkEligibility(User user, FlatType flatType) throws NoSuchElementException;

    /**
     * Checks if a user is eligible to apply for a specific project,
     * considering their eligibility for *any* flat type offered by the project.
     * Also checks if they already have an active application.
     *
     * @param user The User (typically Applicant).
     * @param project The Project.
     * @return true if eligible to apply for the project, false otherwise.
     * @throws NoSuchElementException if user or project data is invalid.
     */
    boolean checkEligibility(User user, Project project) throws NoSuchElementException;
}