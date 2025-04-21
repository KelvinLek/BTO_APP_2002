package service;

import entity.Applicant;
import entity.Project;
import entity.User; // General user for filtering context if needed

import java.util.List;
import java.util.Map;


/**
 * Defines project viewing capabilities specifically for Applicants.
 * Ensures only visible and eligible projects are shown.
 */
public interface IApplicantProjectView {

    /**
     * Retrieves a list of BTO projects visible to a specific applicant,
     * considering project visibility settings and applicant eligibility rules
     * (age, marital status vs flat types offered).
     *
     * @param applicant The Applicant viewing the projects.
     * @return A List of Project objects available and eligible for the applicant.
     */
    List<Project> viewVisibleEligibleProjects(Applicant applicant);

    /**
     * Retrieves the details of a single project by its ID,
     * *only if* it's currently visible and the applicant is potentially eligible.
     * (Or alternatively, allow viewing if the applicant has applied to it, regardless of visibility).
     *
     * @param projectId The ID of the project to view.
     * @param applicant The Applicant viewing the project (for eligibility/visibility check).
     * @return The Project object, or null if not found or not accessible/eligible for this applicant.
     * @throws SecurityException if access is denied based on rules.
     */
    Project viewApplicantProjectById(String projectId, Applicant applicant) throws SecurityException;

    /**
     * Filters visible and eligible projects based on given criteria.
     *
     * @param filters   A Map containing filter criteria (e.g., key="neighbourhood", value="Yishun").
     * @param applicant The Applicant performing the filter (to apply visibility/eligibility).
     * @return A List of Project objects matching the filters and visibility/eligibility rules.
     */
    List<Project> filterApplicantProjects(Map<String, String> filters, Applicant applicant);

}