package service;

import entity.Project;
import entity.User;

import java.util.List;
import java.util.Map;

/**
 * Consolidated interface for project viewing capabilities across all user types.
 * This replaces the separate IApplicantProjectView, IManagerProjectView and IOfficerProjectView.
 */
public interface IProjectView {

    /**
     * Retrieves the details of a project by its ID.
     * The behavior is customized based on the user viewing the project.
     *
     * @param projectId The ID of the project to view.
     * @param user The User viewing the project.
     * @return The Project object, or null if not found or not authorized.
     */
    Project viewProjectById(String projectId, User user);

    /**
     * Filters projects based on given criteria.
     * The filter behavior is customized based on the user role.
     *
     * @param filters A Map containing filter criteria (e.g., key="neighbourhood", value="Yishun").
     * @param user The User performing the filter.
     * @return A List of Project objects matching the filters based on user permissions.
     */
    List<Project> filterAllProjects(Map<String, String> filters, User user);

    /**
     * Retrieves projects that are visible to the specified user.
     * - For Applicants: Returns eligible projects
     * - For Managers: Returns all projects
     * - For Officers: Returns assigned projects
     *
     * @param user The User viewing the projects.
     * @return A List of Project objects relevant to the user.
     */
    List<Project> viewProjectsByUser(User user);
}