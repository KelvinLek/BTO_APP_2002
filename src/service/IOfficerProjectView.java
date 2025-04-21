package service;

import entity.HdbOfficer;
import entity.Project;

import java.util.List;
import java.util.UUID;

/**
 * Defines project viewing capabilities specifically for HDB Officers.
 * Allows viewing assigned projects regardless of visibility.
 */
public interface IOfficerProjectView {

    /**
     * Retrieves the details of a project that the officer is *assigned* to handle.
     * Access should be granted regardless of the project's public visibility setting.
     *
     * @param projectId  The ID of the project to view.
     * @param officer    The HdbOfficer viewing the project (to verify assignment).
     * @return The Project object if found and the officer is assigned, null otherwise.
     * @throws SecurityException if the officer is not assigned to this project.
     */
    Project viewAssignedProjectDetails(String projectId, HdbOfficer officer) throws SecurityException;

    /**
     * Retrieves the list of projects the officer is currently assigned to and approved for.
     *
     * @param officer The HdbOfficer.
     * @return A list of assigned Projects.
     */
    List<Project> viewMyAssignedProjects(HdbOfficer officer);

    // Note: Officers might also need to view generally available projects like applicants.
    // If so, HdbOfficerService could also implement IApplicantProjectView,
    // or we could create a shared IGeneralProjectView interface.
    // For now, focusing on their specific capability.
}