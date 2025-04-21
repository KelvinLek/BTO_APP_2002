package controller;

import entity.*;
import pub_enums.*;
import service.*;
import java.util.*; // For List, Map, UUID

/**
 * Controller for HDB Manager actions. Extends UserController.
 * Requires an HdbManagerService instance.
 */
public class HdbManagerController extends UserController {

    private final HdbManagerService managerService; // Concrete type or interfaces

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
        } catch (IllegalArgumentException | SecurityException e) {
            System.err.println("Project Creation Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during project creation: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles editing an existing project.
     * @param manager        The logged-in HdbManager (for permission check).
     * @param projectId      The ID of the project to edit.
     * @param updatedDetails Map containing fields to update.
     * @return true if successful, false otherwise.
     */
    public boolean handleEditProject(HdbManager manager, String projectId, Map<String, Object> updatedDetails) {
        if (manager == null || projectId == null || updatedDetails == null || updatedDetails.isEmpty()) {
            System.out.println("Error: Missing manager, project ID, or update details.");
            return false;
        }
        try {
            // View layer responsible for collecting updates into the map
            boolean success = managerService.editProject(projectId, updatedDetails, manager);
            if (success) {
                System.out.println("Project " + projectId + " updated successfully.");
                return true;
            } else {
                System.out.println("Project update failed or no changes were made."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | SecurityException | IllegalArgumentException e) {
            System.err.println("Project Edit Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during project edit: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles deleting an existing project.
     * @param manager   The logged-in HdbManager (for permission check).
     * @param projectId The ID of the project to delete.
     * @return true if successful, false otherwise.
     */
    public boolean handleDeleteProject(HdbManager manager, String projectId) {
        if (manager == null || projectId == null) {
            System.out.println("Error: Missing manager or project ID.");
            return false;
        }
        try {
            // Optional: Confirm deletion in View layer
            boolean success = managerService.deleteProject(projectId, manager);
            if (success) {
                System.out.println("Project " + projectId + " deleted successfully.");
                return true;
            } else {
                System.out.println("Project deletion failed."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | SecurityException | IllegalStateException e) {
            System.err.println("Project Deletion Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during project deletion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles toggling the visibility of a project.
     * @param manager   The logged-in HdbManager.
     * @param projectId The ID of the project.
     * @param isVisible The desired visibility state (true for visible, false for hidden).
     * @return true if successful, false otherwise.
     */
    public boolean handleToggleProjectVisibility(HdbManager manager, String projectId, boolean isVisible) {
        if (manager == null || projectId == null) {
            System.out.println("Error: Missing manager or project ID.");
            return false;
        }
        try {
            boolean success = managerService.toggleProjectVisibility(projectId, isVisible, manager);
            if (success) {
                System.out.println("Project " + projectId + " visibility set to " + isVisible + ".");
                return true;
            } else {
                System.out.println("Project visibility toggle failed or no change was needed."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | SecurityException e) {
            System.err.println("Visibility Toggle Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during visibility toggle: " + e.getMessage());
            return false;
        }
    }


    // --- Approval Processing ---

    /**
     * Handles processing an HDB Officer's registration request for a project.
     * @param manager   The logged-in HdbManager.
     * @param officerId The NRIC of the HdbOfficer.
     * @param projectId The ID of the Project.
     * @param approve   True to approve, false to reject.
     * @return true if processing was successful, false otherwise.
     */
    public boolean handleProcessOfficerRegistration(HdbManager manager, String officerId, String projectId, boolean approve) {
        if (manager == null || officerId == null || projectId == null) {
            System.out.println("Error: Missing manager, officer ID, or project ID.");
            return false;
        }
        try {
            // Need to fetch Officer and Project objects first to pass to the service
            // In a real app with repos, fetch here. Using service placeholders for now.
            User user = managerService.findUserByNricPlaceholder(officerId); // Need access to user finding
            Project project = managerService.findProjectByIdPlaceholder(projectId); // Need access to project finding

            if (!(user instanceof HdbOfficer)) {
                System.out.println("Error: User with NRIC " + officerId + " is not an HDB Officer.");
                return false;
            }
            if (project == null) {
                System.out.println("Error: Project with ID " + projectId + " not found.");
                return false;
            }
            HdbOfficer officer = (HdbOfficer) user;


            boolean success = managerService.processOfficerRegistration(officer, project, manager, approve);
            if(success) {
                System.out.println("Officer " + officerId + " registration for project " + projectId + " processed: " + (approve ? "Approved" : "Rejected"));
                return true;
            } else {
                System.out.println("Processing officer registration failed."); // Service gives reason
                return false;
            }

        } catch (NoSuchElementException | SecurityException | IllegalStateException e) {
            System.err.println("Officer Registration Processing Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred processing officer registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles processing an Applicant's application approval/rejection.
     * @param manager       The logged-in HdbManager.
     * @param applicationId The ID of the Application.
     * @param approve       True to approve, false to reject.
     * @return true if processing successful, false otherwise.
     */
    public boolean handleProcessApplicationApproval(HdbManager manager, String applicationId, boolean approve) {
        if (manager == null || applicationId == null) {
            System.out.println("Error: Missing manager or application ID.");
            return false;
        }
        try {
            // Fetch Application object
            Application application = managerService.findApplicationByIdPlaceholder(applicationId); // Need access
            if (application == null) {
                System.out.println("Error: Application with ID " + applicationId + " not found.");
                return false;
            }

            boolean success = managerService.processApplicationApproval(application, manager, approve);
            if(success) {
                System.out.println("Application " + applicationId + " processed: " + (approve ? "Approved (Success)" : "Rejected"));
                return true;
            } else {
                System.out.println("Processing application approval failed."); // Service gives reason
                return false;
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            System.err.println("Application Approval Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred processing application approval: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles processing an Applicant's withdrawal request (approval/rejection).
     * @param manager       The logged-in HdbManager.
     * @param applicationId The ID of the Application (status should be WITHDRAW_PENDING).
     * @param approve       True to approve withdrawal, false to reject withdrawal.
     * @return true if processing successful, false otherwise.
     */
    public boolean handleProcessWithdrawal(HdbManager manager, String applicationId, boolean approve) {
        if (manager == null || applicationId == null) {
            System.out.println("Error: Missing manager or application ID.");
            return false;
        }
        try {
            Application application = managerService.findApplicationByIdPlaceholder(applicationId); // Need access
            if (application == null) {
                System.out.println("Error: Application with ID " + applicationId + " not found.");
                return false;
            }
            if (application.getStatus() != ApplStatus.WITHDRAW_PENDING) {
                System.out.println("Error: Application " + applicationId + " is not pending withdrawal.");
                return false;
            }

            boolean success;
            if (approve) {
                success = managerService.approveWithdrawal(application, manager);
            } else {
                success = managerService.rejectWithdrawal(application, manager);
            }

            if (success) {
                System.out.println("Withdrawal request for application " + applicationId + " processed: " + (approve ? "Approved" : "Rejected"));
                return true;
            } else {
                System.out.println("Processing withdrawal request failed."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            System.err.println("Withdrawal Processing Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred processing withdrawal: " + e.getMessage());
            return false;
        }
    }

    // --- Viewing & Reporting ---

    /**
     * Handles viewing all projects (manager perspective).
     * @param manager The logged-in HdbManager.
     * @return List of all Project objects.
     */
    public List<Project> handleViewAllProjects(HdbManager manager) {
        if (manager == null) { System.out.println("Error: Manager context missing."); return Collections.emptyList(); }
        try {
            List<Project> projects = managerService.viewAllProjects(manager);
            if (projects.isEmpty()) { System.out.println("No projects found in the system."); }
            // View handles display
            return projects;
        } catch (Exception e) {
            System.err.println("An error occurred fetching all projects: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles viewing all enquiries (manager perspective).
     * @param manager The logged-in HdbManager.
     * @return List of all Enquiry objects.
     */
    public List<Enquiry> handleViewAllEnquiries(HdbManager manager) {
        if (manager == null) { System.out.println("Error: Manager context missing."); return Collections.emptyList(); }
        try {
            List<Enquiry> enquiries = managerService.viewAllEnquiries(manager);
            if (enquiries.isEmpty()) { System.out.println("No enquiries found in the system."); }
            // View handles display
            return enquiries;
        } catch (Exception e) {
            System.err.println("An error occurred fetching all enquiries: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * Handles generating a booking report based on filters.
     * @param manager The logged-in HdbManager.
     * @param filters Map of filter criteria (e.g., "projectName", "flatType", "maritalStatus").
     * @return List of report items (currently Object, could be specific ReportItem class).
     */
    public List<Object> handleGenerateBookingReport(HdbManager manager, Map<String, String> filters) {
        if (manager == null || filters == null) {
            System.out.println("Error: Missing manager context or filter criteria.");
            return Collections.emptyList();
        }
        try {
            // View layer collects filters into the map
            List<Object> reportData = managerService.generateBookingReport(filters);
            if (reportData.isEmpty()) {
                System.out.println("No booking records found matching the specified criteria.");
            }
            // View layer is responsible for displaying the report data nicely
            return reportData;
        } catch (IllegalArgumentException e) {
            System.err.println("Report Generation Error: Invalid filter value - " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during report generation: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles replying to any enquiry as a manager.
     * @param manager    The logged-in HdbManager.
     * @param enquiryId  The ID of the enquiry to reply to.
     * @param replyText  The reply message.
     * @return true if successful, false otherwise.
     */
    public boolean handleManagerReplyToEnquiry(HdbManager manager, String enquiryId, String replyText) {
        if (manager == null || enquiryId == null || replyText == null || replyText.trim().isEmpty()) {
            System.out.println("Error: Missing manager, enquiry ID, or reply text.");
            return false;
        }
        try {
            // Fetch enquiry object
            Enquiry enquiry = managerService.viewAnyEnquiryById(enquiryId, manager); // Manager access view
            if (enquiry == null) {
                System.out.println("Enquiry with ID " + enquiryId + " not found.");
                return false;
            }
            if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()) {
                System.out.println("This enquiry has already been replied to.");
                // Ask if overwrite? View layer decision. For now, prevent.
                return false;
            }

            boolean success = managerService.replyToEnquiry(enquiry, replyText, manager);
            if(success) {
                System.out.println("Manager reply submitted successfully for enquiry ID: " + enquiryId);
                return true;
            } else {
                System.out.println("Failed to submit manager reply."); // Service gives reason
                return false;
            }
        } catch (NoSuchElementException | SecurityException e) {
            System.err.println("Manager Reply Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred submitting manager reply: " + e.getMessage());
            return false;
        }
    }

}