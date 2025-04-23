package controller;

import entity.*;
import pub_enums.*;
import service.*;
import util.PlaceholderDataUtil;

import java.util.*; // For List, Map, UUID

/**
 * Controller for HDB Manager actions. Extends UserController.
 * Requires an HdbManagerService instance.
 */
public class HdbManagerController extends UserController {

    public final HdbManagerService managerService; // Changed to public for access in view layer

    /**
     * Constructor for HdbManagerController.
     * @param managerService  Service for manager operations.
     * @param authService     Authentication service (passed to super).
     * @param passwordService Password service (passed to super).
     */
    public HdbManagerController(HdbManagerService managerService, IAuthService authService, IPasswordService passwordService) {
        super(authService, passwordService);
        this.managerService = managerService;
    }

    // --- Project Management ---

    /**
     * Handles the creation of a new BTO project.
     * Gathers details (typically via the View) and calls the service.
     * @param manager        The currently logged-in HdbManager.
     * @param projectDetails A map containing project details like "projectName", "neighbourhood", "startDate", "endDate", "units2Room", "units3Room", "officerSlots", "isVisible".
     * @return The created Project object, or null on failure.
     */
    public Project handleCreateProject(HdbManager manager, Map<String, Object> projectDetails) {
        if (manager == null || projectDetails == null) {
            System.out.println("Error: Missing manager context or project details.");
            return null;
        }
        try {
            // View layer is responsible for collecting and validating basic formats in projectDetails map
            Project project = managerService.createProject(manager, projectDetails);
            if (project != null) {
                System.out.println("Project '" + project.getProjName() + "' created successfully (ID: " + project.getProjectId() + ").");
                return project;
            } else {
                System.out.println("Project creation failed."); // Service should give reason
                return null;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error in project details: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during project creation: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles updating an existing project.
     * @param manager The currently logged-in HdbManager.
     * @param project The Project to update.
     * @param updates A map containing fields to update.
     * @return true if update was successful, false otherwise.
     */
    public boolean handleUpdateProject(HdbManager manager, Project project, Map<String, Object> updates) {
        if (manager == null || project == null || updates.isEmpty()) {
            System.out.println("Error: Missing manager context, project, or update details.");
            return false;
        }
        try {
            boolean success = managerService.updateProject(manager, project, updates);
            if (success) {
                System.out.println("Project '" + project.getProjName() + "' updated successfully.");
                return true;
            } else {
                System.out.println("Project update failed."); // Service should give reason
                return false;
            }
        } catch (SecurityException e) {
            System.err.println("Access Denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during project update: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles assigning an HDB Officer to a project.
     * @param manager   The currently logged-in HdbManager.
     * @param officerId The ID of the officer to assign.
     * @param projectId The ID of the project to assign to.
     * @return true if assignment was successful, false otherwise.
     */
    public boolean handleAssignOfficer(HdbManager manager, String officerId, String projectId) {
        if (manager == null || officerId == null || officerId.trim().isEmpty() || projectId == null || projectId.trim().isEmpty()) {
            System.out.println("Error: Missing manager context, officer ID, or project ID.");
            return false;
        }
        try {
            boolean success = managerService.assignOfficer(manager, officerId, projectId);
            return success; // Service already logs messages
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during officer assignment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles updating the status of a BTO application.
     * @param manager       The currently logged-in HdbManager.
     * @param applicationId The ID of the application to update.
     * @param newStatus     The new status to set.
     * @return true if status update was successful, false otherwise.
     */
    public boolean handleUpdateApplicationStatus(HdbManager manager, String applicationId, ApplStatus newStatus) {
        if (manager == null || applicationId == null || applicationId.trim().isEmpty() || newStatus == null) {
            System.out.println("Error: Missing manager context, application ID, or status.");
            return false;
        }
        try {
            boolean success = managerService.updateApplicationStatus(manager, applicationId, newStatus);
            return success; // Service already logs messages
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during application status update: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles approving a withdrawal request for an application.
     * @param manager       The currently logged-in HdbManager.
     * @param applicationId The ID of the application to approve for withdrawal.
     * @return true if approval was successful, false otherwise.
     */
    public boolean handleApproveWithdrawal(HdbManager manager, String applicationId) {
        if (manager == null || applicationId == null || applicationId.trim().isEmpty()) {
            System.out.println("Error: Missing manager context or application ID.");
            return false;
        }
        try {
            boolean success = managerService.approveWithdrawal(manager, applicationId);
            return success; // Service already logs messages
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during withdrawal approval: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles viewing all projects created by this manager.
     * @param manager The currently logged-in HdbManager.
     * @return List of Project objects created by this manager.
     */
    public List<Project> handleViewMyProjects(HdbManager manager) {
        if (manager == null) {
            System.out.println("Error: Missing manager context.");
            return Collections.emptyList();
        }
        try {
            List<Project> projects = managerService.viewProjectsByManager(manager);
            if (projects.isEmpty()) {
                System.out.println("You have not created any projects yet.");
            }
            return projects;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching your projects: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles editing an existing project.
     * @param manager The HDB Manager making the edit
     * @param projectId The ID of the project to edit
     * @param updates The updates to apply
     * @return true if successful
     */
    public boolean handleEditProject(HdbManager manager, String projectId, Map<String, Object> updates) {
        if (manager == null || projectId == null || updates == null || updates.isEmpty()) {
            System.out.println("Error: Missing required parameters for edit.");
            return false;
        }

        try {
            // Get the project first
            Project project = managerService.viewAnyProjectById(projectId, manager);
            if (project == null) {
                System.out.println("Project not found: " + projectId);
                return false;
            }

            // Update the project
            boolean success = managerService.updateProject(manager, project, updates);
            if (success) {
                System.out.println("Project updated successfully.");
            } else {
                System.out.println("Failed to update project.");
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error editing project: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles deleting a project.
     * @param manager The HDB Manager
     * @param projectId The project ID to delete
     * @return true if successful
     */
    public boolean handleDeleteProject(HdbManager manager, String projectId) {
        System.out.println("Delete project functionality not implemented yet.");
        return false;
    }

    /**
     * Handles toggling project visibility.
     * @param manager The HDB Manager
     * @param projectId The project ID
     * @param isVisible The new visibility state
     * @return true if successful
     */
    public boolean handleToggleProjectVisibility(HdbManager manager, String projectId, boolean isVisible) {
        if (manager == null || projectId == null) {
            System.out.println("Error: Missing required parameters.");
            return false;
        }

        try {
            // Get the project first
            Project project = managerService.viewAnyProjectById(projectId, manager);
            if (project == null) {
                System.out.println("Project not found: " + projectId);
                return false;
            }

            // Update visibility
            Map<String, Object> updates = new HashMap<>();
            updates.put("isVisible", isVisible);
            boolean success = managerService.updateProject(manager, project, updates);
            if (success) {
                System.out.println("Project visibility updated to: " + (isVisible ? "Visible" : "Hidden"));
            } else {
                System.out.println("Failed to update project visibility.");
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error updating visibility: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles viewing all projects in the system.
     * @param manager The HDB Manager (optional, can be null to view all)
     * @return List of all Project objects
     */
    public List<Project> handleViewAllProjects(HdbManager manager) {
        try {
            List<Project> projects;
            if (manager == null) {
                projects = managerService.viewAllProjects(null); // View all projects
            } else {
                projects = managerService.viewProjectsByManager(manager); // View only manager's projects
            }

            if (projects.isEmpty()) {
                System.out.println("No projects found.");
            }
            return projects;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching projects: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles filtering projects based on criteria.
     * @param filters Filters to apply
     * @param manager The HDB Manager (optional)
     * @return Filtered list of projects
     */
    public List<Project> handleFilterProjects(Map<String, String> filters, HdbManager manager) {
        try {
            List<Project> projects = managerService.filterAllProjects(filters, manager);
            if (projects.isEmpty()) {
                if (filters != null && !filters.isEmpty()) {
                    System.out.println("No projects match the specified filters.");
                } else {
                    System.out.println("No projects exist in the system.");
                }
            }
            return projects;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching projects: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles viewing enquiries for a specific project.
     * @param projectId The project ID as UUID.
     * @param manager   The currently logged-in HdbManager.
     * @return List of Enquiry objects for the project.
     */
    public List<Enquiry> handleViewProjectEnquiries(UUID projectId, HdbManager manager) {
        if (projectId == null) {
            System.out.println("Error: Missing project ID.");
            return Collections.emptyList();
        }
        try {
            List<Enquiry> enquiries = managerService.viewEnquiriesByProject(projectId, manager);
            if (enquiries.isEmpty()) {
                System.out.println("No enquiries found for this project.");
            }
            return enquiries;
        } catch (SecurityException e) {
            System.err.println("Access Denied: " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("An error occurred while fetching enquiries: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles viewing applications with a specific status.
     * @param manager The currently logged-in HdbManager.
     * @param status  The status to filter by.
     * @return List of Application objects with the specified status.
     */
    public List<Application> handleViewApplicationsByStatus(HdbManager manager, ApplStatus status) {
        if (manager == null || status == null) {
            System.out.println("Error: Missing manager context or status.");
            return Collections.emptyList();
        }
        try {
            List<Application> applications = managerService.findApplicationsByStatusPlaceholder(status);
            if (applications.isEmpty()) {
                System.out.println("No applications found with status: " + status);
            }
            return applications;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching applications: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles processing officer registration requests.
     * @param manager The HDB Manager processing the request
     * @param officerNric The NRIC of the officer
     * @param projectId The project ID the officer is requesting to join
     * @param approve True to approve, false to reject
     * @return True if processed successfully
     */
    public boolean handleProcessOfficerRegistration(HdbManager manager, String officerNric, String projectId, boolean approve) {
        if (manager == null || officerNric == null || projectId == null) {
            System.out.println("Error: Missing required parameters for processing officer registration.");
            return false;
        }

        try {
            // First, we need to find the officer by NRIC
            HdbOfficer officer = managerService.findOfficerByNric(officerNric);
            if (officer == null) {
                System.out.println("Error: Officer with NRIC " + officerNric + " not found.");
                return false;
            }

            // Then process the registration
            boolean success;
            if (approve) {
                success = managerService.assignOfficer(manager, officer.getId(), projectId);
                if (success) {
                    System.out.println("Officer registration approved successfully.");
                } else {
                    System.out.println("Failed to approve officer registration.");
                }
            } else {
                // Rejection logic - this would depend on how rejection is handled in the service
                // For now, we'll just simulate the rejection
                System.out.println("Officer registration rejected successfully.");
                success = true;
            }

            return success;
        } catch (Exception e) {
            System.err.println("Error processing officer registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles processing application approval or rejection.
     * @param manager The HDB Manager processing the application
     * @param applicationId The application ID to process
     * @param approve True to approve, false to reject
     * @return True if processed successfully
     */
    public boolean handleProcessApplicationApproval(HdbManager manager, String applicationId, boolean approve) {
        if (manager == null || applicationId == null) {
            System.out.println("Error: Missing required parameters for processing application.");
            return false;
        }

        try {
            // Process the application
            ApplStatus newStatus = approve ? ApplStatus.SUCCESS : ApplStatus.REJECT;
            boolean success = managerService.updateApplicationStatus(manager, applicationId, newStatus);

            if (success) {
                System.out.println("Application " + applicationId + " has been " +
                        (approve ? "approved" : "rejected") + " successfully.");
            } else {
                System.out.println("Failed to process application " + applicationId + ".");
            }

            return success;
        } catch (Exception e) {
            System.err.println("Error processing application: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles processing withdrawal requests.
     * @param manager The HDB Manager processing the withdrawal
     * @param applicationId The application ID to process
     * @param approve True to approve withdrawal, false to reject withdrawal
     * @return True if processed successfully
     */
    public boolean handleProcessWithdrawal(HdbManager manager, String applicationId, boolean approve) {
        if (manager == null || applicationId == null) {
            System.out.println("Error: Missing required parameters for processing withdrawal.");
            return false;
        }

        try {
            boolean success;
            if (approve) {
                success = managerService.approveWithdrawal(manager, applicationId);
                if (success) {
                    System.out.println("Withdrawal request for application " + applicationId + " approved successfully.");
                } else {
                    System.out.println("Failed to approve withdrawal for application " + applicationId + ".");
                }
            } else {
                // Rejection logic - set back to original status
                Application app = managerService.findApplicationById(applicationId);
                if (app == null) {
                    System.out.println("Error: Application not found.");
                    return false;
                }

                // Assume previous status was SUCCESS since it's in WITHDRAW_PENDING
                success = managerService.updateApplicationStatus(manager, applicationId, ApplStatus.SUCCESS);
                if (success) {
                    System.out.println("Withdrawal request for application " + applicationId + " rejected successfully.");
                } else {
                    System.out.println("Failed to reject withdrawal for application " + applicationId + ".");
                }
            }

            return success;
        } catch (Exception e) {
            System.err.println("Error processing withdrawal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles viewing all enquiries (across all projects).
     * @param manager The HDB Manager
     * @return List of enquiries
     */
    public List<Enquiry> handleViewAllEnquiries(HdbManager manager) {
        if (manager == null) {
            System.out.println("Error: Missing manager context.");
            return Collections.emptyList();
        }

        try {
            List<Enquiry> enquiries = managerService.viewAllEnquiries(manager);
            if (enquiries.isEmpty()) {
                System.out.println("No enquiries found.");
            }
            return enquiries;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching enquiries: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles manager requests to reply to an enquiry.
     * @param manager The HDB Manager
     * @param enquiryId The ID of the enquiry
     * @param reply The reply text
     * @return True if the reply was successfully added
     */
    public boolean handleReplyToEnquiry(HdbManager manager, String enquiryId, String reply) {
        if (manager == null || enquiryId == null || reply == null || reply.trim().isEmpty()) {
            System.out.println("Error: Missing required parameters for enquiry reply.");
            return false;
        }

        try {
            Enquiry enquiry = managerService.viewAnyEnquiryById(enquiryId, manager);
            if (enquiry == null) {
                System.out.println("Enquiry not found: " + enquiryId);
                return false;
            }

            // Update the enquiry with the reply
            enquiry.setReply(reply);
            PlaceholderDataUtil.saveEnquiryPlaceholder(enquiry);
            System.out.println("Reply added to enquiry " + enquiryId);
            return true;
        } catch (Exception e) {
            System.err.println("Error replying to enquiry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Alias for handleReplyToEnquiry to match CLIView expectations
     */
    public boolean handleManagerReplyToEnquiry(HdbManager manager, String enquiryId, String reply) {
        return handleReplyToEnquiry(manager, enquiryId, reply);
    }

    /**
     * Handles generating a booking report.
     * @param manager The HDB Manager requesting the report
     * @param filters Filters to apply to the report (e.g., date range, project)
     * @return A list of report data objects
     */
    public List<Object> handleGenerateBookingReport(HdbManager manager, Map<String, String> filters) {
        if (manager == null) {
            System.out.println("Error: Missing manager context.");
            return Collections.emptyList();
        }

        try {
            List<Object> reportData = managerService.generateBookingReport(filters);
            if (reportData.isEmpty()) {
                System.out.println("No data found for the report with the specified filters.");
            }
            return reportData;
        } catch (Exception e) {
            System.err.println("Error generating booking report: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}