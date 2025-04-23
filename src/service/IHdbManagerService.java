package service;

import entity.HdbManager;
import entity.Project;
import pub_enums.ApplStatus;
import java.util.List;
import java.util.Map;

/**
 * Interface defining HDB Manager specific operations.
 * This supplements the IManagerService interface with additional methods.
 */
public interface IHdbManagerService {
    
    /**
     * Creates a new BTO project with the given details.
     * 
     * @param manager The HDB Manager creating the project
     * @param projectDetails Map containing project details
     * @return The newly created Project
     */
    Project createProject(HdbManager manager, Map<String, Object> projectDetails);
    
    /**
     * Updates an existing project with new details.
     * 
     * @param manager The HDB Manager updating the project
     * @param project The Project to update
     * @param updates Map containing the updates to apply
     * @return true if update was successful
     */
    boolean updateProject(HdbManager manager, Project project, Map<String, Object> updates);
    
    /**
     * Assigns an officer to a project.
     * 
     * @param manager The HDB Manager making the assignment
     * @param officerId The ID of the officer to assign
     * @param projectId The ID of the project
     * @return true if assignment successful
     */
    boolean assignOfficer(HdbManager manager, String officerId, String projectId);
    
    /**
     * Updates an application's status.
     * 
     * @param manager The HDB Manager updating the status
     * @param applicationId The application ID
     * @param newStatus The new status to set
     * @return true if update successful
     */
    boolean updateApplicationStatus(HdbManager manager, String applicationId, ApplStatus newStatus);
    
    /**
     * Approves a withdrawal request.
     * 
     * @param manager The HDB Manager approving the request
     * @param applicationId The application ID
     * @return true if approval successful
     */
    boolean approveWithdrawal(HdbManager manager, String applicationId);
    
    /**
     * Gets all projects created by a specific manager.
     * 
     * @param manager The HDB Manager
     * @return List of projects created by the manager
     */
    List<Project> viewProjectsByManager(HdbManager manager);
    
    /**
     * Filters projects based on criteria.
     * 
     * @param filters Filter criteria
     * @param manager Optional manager to filter by (can be null)
     * @return Filtered list of projects
     */
    List<Project> filterAllProjects(Map<String, String> filters, HdbManager manager);
}