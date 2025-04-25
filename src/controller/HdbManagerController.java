package controller;

import entity.*;
import pub_enums.*;
import service.HdbManagerService;
import service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Controller for HDB Manager-specific operations
 */
public class HdbManagerController extends UserController {
    private HdbManagerService managerService;

    public HdbManagerController(HdbManagerService managerService, UserService userService) {
        super(userService);
        this.managerService = managerService;
    }

    /**
     * Create a new project
     * 
     * @param manager The manager creating the project
     * @param projectDetails Map containing project details (name, neighbourhood, dates, etc.)
     * @return The created project or null if creation failed
     */
    public Project createProject(HdbManager manager, Map<String, Object> projectDetails) {
        if (manager == null || projectDetails == null) {
            System.out.println("Error: Manager or project details missing.");
            return null;
        }
        
        try {
            Project project = managerService.createProject(manager, projectDetails);
            System.out.println("Project created successfully: " + project.getProjName());
            return project;
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating project: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error creating project: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update an existing project
     * 
     * @param manager The manager updating the project
     * @param project The project to update
     * @param updates Map containing the fields to update
     * @return true if update successful, false otherwise
     */
    public boolean updateProject(HdbManager manager, Project project, Map<String, Object> updates) {
        if (manager == null || project == null || updates == null) {
            System.out.println("Error: Manager, project, or updates information missing.");
            return false;
        }
        
        boolean result = managerService.updateProject(manager, project, updates);
        
        if (result) {
            System.out.println("Project updated successfully.");
        } else {
            System.out.println("Failed to update project. Please try again.");
        }
        
        return result;
    }

    public void deleteProject(HdbManager manager, Project project) {
        if (manager == null || project == null) {
            System.out.println("Error: Manager or project missing.");
        }
        else {
            managerService.deleteProject(manager, project);
            System.out.println("Project deleted successfully: " + project.getProjName());
        }
    }

    /**
     * Assign an officer to a project
     * 
     * @param manager The manager making the assignment
     * @param officerId The ID of the officer to assign
     * @param projectId The ID of the project to assign to
     * @return true if assignment successful, false otherwise
     */
    public boolean assignOfficer(HdbManager manager, String officerId, String projectId, boolean confirm) {
        if (manager == null || officerId == null || projectId == null) {
            System.out.println("Error: Manager, officer ID, or project ID information missing.");
            return false;
        }
        
        boolean result = managerService.assignOfficer(manager, officerId, projectId, confirm);
        
        if (result) {
            System.out.println("Officer assigned to project successfully.");
        } else {
            System.out.println("Failed to assign officer to project. Please try again.");
        }
        
        return result;
    }

    /**
     * Update application status
     * 
     * @param manager The manager updating the status
     * @param applicationId The ID of the application to update
     * @param newStatus The new status to set
     * @return true if update successful, false otherwise
     */
    public boolean updateApplicationStatus(HdbManager manager, String applicationId, ApplStatus newStatus) {
        if (manager == null || applicationId == null || newStatus == null) {
            System.out.println("Error: Manager, application ID, or status information missing.");
            return false;
        }
        
        boolean result = managerService.updateApplicationStatus(manager, applicationId, newStatus);
        
        if (result) {
            System.out.println("Application status updated successfully.");
        } else {
            System.out.println("Failed to update application status. Please try again.");
        }
        
        return result;
    }

    /**
     * Approve withdrawal of an application
     * 
     * @param manager The manager approving the withdrawal
     * @param applicationId The ID of the application to withdraw
     * @return true if approval successful, false otherwise
     */
    public boolean approveWithdrawal(HdbManager manager, String applicationId) {
        if (manager == null || applicationId == null) {
            System.out.println("Error: Manager or application ID information missing.");
            return false;
        }
        
        boolean result = managerService.approveWithdrawal(manager, applicationId);
        
        if (result) {
            System.out.println("Withdrawal approved successfully.");
        } else {
            System.out.println("Failed to approve withdrawal. Please try again.");
        }
        
        return result;
    }

    /**
     * Reject withdrawal of an application
     * 
     * @param manager The manager rejecting the withdrawal
     * @param applicationId The ID of the application
     * @return true if rejection successful, false otherwise
     */
    public boolean rejectWithdrawal(HdbManager manager, String applicationId) {
        if (manager == null || applicationId == null) {
            System.out.println("Error: Manager or application ID information missing.");
            return false;
        }
        
        boolean result = managerService.rejectWithdrawal(manager, applicationId);
        
        if (result) {
            System.out.println("Withdrawal request rejected successfully.");
        } else {
            System.out.println("Failed to reject withdrawal request. Please try again.");
        }
        
        return result;
    }

    /**
     * View projects managed by the manager
     * 
     * @param manager The manager viewing projects
     * @return List of managed projects
     */
    public List<Project> viewManagedProjects(HdbManager manager) {
        if (manager == null) {
            System.out.println("Error: Manager information missing.");
            return List.of();
        }
        
        List<Project> projects = managerService.viewProjectsByUser(manager);
        
        if (projects.isEmpty()) {
            System.out.println("You don't have any managed projects.");
        }
        
        return projects;
    }

    /**
     * View all projects in the system
     * 
     * @param manager The manager viewing projects
     * @return List of all projects
     */
    public List<Project> viewAllProjects(HdbManager manager) {
        if (manager == null) {
            System.out.println("Error: Manager information missing.");
            return List.of();
        }
        
        List<Project> projects = managerService.filterAllProjects(Map.of(), manager);
        
        if (projects.isEmpty()) {
            System.out.println("No projects found in the system.");
        }
        
        return projects;
    }

    /**
     * View details of a specific project
     * 
     * @param projectId The ID of the project to view
     * @param manager The manager viewing the project
     * @return The project details or null if not found
     */
    public Project viewProjectDetails(String projectId, HdbManager manager) {
        if (projectId == null || manager == null) {
            System.out.println("Error: Project ID or manager information missing.");
            return null;
        }
        
        Project project = managerService.viewProjectById(projectId, manager);
        
        if (project == null) {
            System.out.println("Project not found.");
        }
        
        return project;
    }

    /**
     * Filter projects based on criteria
     * 
     * @param filters Map of filter criteria
     * @param manager The manager filtering projects
     * @return List of filtered projects
     */
    public List<Project> filterProjects(Map<String, String> filters, HdbManager manager) {
        if (filters == null || manager == null) {
            System.out.println("Error: Filter criteria or manager information missing.");
            return List.of();
        }
        
        List<Project> projects = managerService.filterAllProjects(filters, manager);
        
        if (projects.isEmpty()) {
            System.out.println("No projects match your filter criteria.");
        }
        
        return projects;
    }

    /**
     * Generate a booking report
     * 
     * @param filters Map of filter criteria for the report
     * @param manager The manager generating the report
     * @return List of report data
     */
    public List<Object> generateBookingReport(Map<String, String> filters, HdbManager manager) {
        if (filters == null || manager == null) {
            System.out.println("Error: Filter criteria or manager information missing.");
            return List.of();
        }
        
        try {
            List<Object> reportData = managerService.generateBookingReport(filters);
            
            if (reportData.isEmpty()) {
                System.out.println("No data found for the report.");
            } else {
                System.out.println("Report generated successfully with " + reportData.size() + " entries.");
            }
            
            return reportData;
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Process an application (approve or reject)
     * 
     * @param applicationId The ID of the application to process
     * @param manager The manager processing the application
     * @param approve true to approve, false to reject
     * @return true if processing successful, false otherwise
     */
    public boolean processApplication(String applicationId, HdbManager manager, boolean approve) {
        if (applicationId == null || manager == null) {
            System.out.println("Error: Application ID or manager information missing.");
            return false;
        }
        
        boolean result = managerService.processApplication(applicationId, manager, approve);
        
        if (result) {
            System.out.println("Application " + (approve ? "approved" : "rejected") + " successfully.");
        } else {
            System.out.println("Failed to process application. Please try again.");
        }
        
        return result;
    }
}