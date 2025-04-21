package service;

import entity.HdbManager;
import entity.Project;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Defines project viewing capabilities specifically for HDB Managers.
 * Allows viewing all projects and filtering.
 */
public interface IManagerProjectView {

    /**
     * Retrieves all BTO projects in the system, regardless of visibility or creator.
     *
     * @param manager The HdbManager performing the action (context).
     * @return A List of all Project objects.
     */
    List<Project> viewAllProjects(HdbManager manager);

    /**
     * Retrieves all projects created by a specific HDB Manager.
     *
     * @param manager The HdbManager whose projects are to be viewed.
     * @return A List of Project objects created by that manager.
     */
    List<Project> viewProjectsByManager(HdbManager manager);

    /**
     * Retrieves the details of any single project by its ID, regardless of visibility.
     *
     * @param projectId The ID of the project to view.
     * @param manager   The HdbManager performing the action (context).
     * @return The Project object, or null if not found.
     */
    Project viewAnyProjectById(UUID projectId, HdbManager manager);

    /**
     * Filters all projects based on given criteria.
     *
     * @param filters A Map containing filter criteria.
     * @param manager The HdbManager performing the filter.
     * @return A List of Project objects matching the filters.
     */
    List<Project> filterAllProjects(Map<String, String> filters, HdbManager manager);
}