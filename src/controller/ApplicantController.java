package controller;

import entity.*;
import pub_enums.*;
import service.*;
import java.util.*; // For List, Map

/**
 * Controller for Applicant-specific actions. Extends UserController for common actions.
 * Requires an ApplicantService instance.
 */
public class ApplicantController extends UserController {

    private final ApplicantService applicantService; // Use concrete type if methods are specific, or interfaces

    /**
     * Constructor for ApplicantController.
     * @param applicantService Service for applicant operations.
     * @param authService      Authentication service (passed to super).
     * @param passwordService  Password service (passed to super).
     */
    public ApplicantController(ApplicantService applicantService, IAuthService authService, IPasswordService passwordService) {
        super(authService, passwordService); // Initialize base controller
        this.applicantService = applicantService;
    }

    /**
     * Handles viewing available BTO projects for the applicant.
     * @param applicant The currently logged-in Applicant.
     * @return List of available Project objects, or empty list on error/no projects.
     */
    public List<Project> handleViewAvailableProjects(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant context is missing.");
            return Collections.emptyList();
        }
        try {
            List<Project> projects = applicantService.viewAvailableProjects(applicant);
            if (projects.isEmpty()) {
                System.out.println("No BTO projects currently available for application or matching your eligibility.");
            }
            // The view layer will handle the actual display of the projects.
            return projects;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching projects: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles applying for a specific project and flat type.
     * @param applicant The currently logged-in Applicant.
     * @param project   The Project being applied for.
     * @param flatType  The desired FlatType.
     * @return true if application submitted successfully, false otherwise.
     */
    public boolean handleApplyForProject(Applicant applicant, Project project, FlatType flatType) {
        if (applicant == null || project == null || flatType == null) {
            System.out.println("Error: Missing applicant, project, or flat type information.");
            return false;
        }
        try {
            boolean success = applicantService.applyForProject(applicant, project, flatType);
            if (success) {
                System.out.println("Application submitted successfully for project: " + project.getProjName());
                return true;
            } else {
                System.out.println("Application submission failed."); // Service layer should provide specific reason
                return false;
            }
        } catch (IllegalArgumentException | NoSuchElementException | SecurityException e) {
            System.err.println("Application Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during application: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles viewing the applicant's current application status.
     * @param applicant The currently logged-in Applicant.
     * @return The Application object or null if none found or error.
     */
    public Application handleGetApplicationStatus(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant context is missing.");
            return null;
        }
        try {
            Application app = applicantService.getApplicationStatus(applicant);
            if (app == null) {
                System.out.println("You do not have an active application.");
            }
            // View layer handles display
            return app;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching application status: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles requesting withdrawal of the applicant's current application.
     * @param applicant The currently logged-in Applicant.
     * @return true if withdrawal request was submitted, false otherwise.
     */
    public boolean handleRequestWithdrawal(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant context is missing.");
            return false;
        }
        try {
            // First, get the current application
            Application app = applicantService.getApplicationStatus(applicant);
            if (app == null) {
                System.out.println("You do not have an application to withdraw.");
                return false;
            }
            if (app.getStatus() == ApplStatus.WITHDRAW_PENDING || app.getStatus() == ApplStatus.WITHDRAW_APPROVED) {
                System.out.println("Application withdrawal is already pending or approved.");
                return false; // Or true, depending on desired idempotent behavior
            }


            // Confirm with user before proceeding (View layer should handle this)
            System.out.println("Are you sure you want to request withdrawal for application ID: " + app.getId() + "? (Y/N)");
            // --- Assume View gets confirmation ---
            boolean confirmed = true; // Placeholder

            if (confirmed) {
                boolean success = applicantService.requestWithdrawal(app);
                if (success) {
                    System.out.println("Withdrawal request submitted successfully.");
                    return true;
                } else {
                    System.out.println("Withdrawal request failed."); // Service layer should provide reason
                    return false;
                }
            } else {
                System.out.println("Withdrawal cancelled.");
                return false;
            }

        } catch (NoSuchElementException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An error occurred during withdrawal request: " + e.getMessage());
            return false;
        }
    }


    // --- Enquiry Handling ---

    /**
     * Handles submitting a new enquiry.
     * @param applicant The logged-in Applicant.
     * @param project   The Project to enquire about.
     * @param message   The enquiry message.
     * @return The created Enquiry object, or null on failure.
     */
    public Enquiry handleSubmitEnquiry(Applicant applicant, Project project, String message) {
        if (applicant == null || project == null || message == null || message.trim().isEmpty()) {
            System.out.println("Error: Missing applicant, project, or message for enquiry.");
            return null;
        }
        try {
            Enquiry enquiry = applicantService.submitEnquiry(applicant, project, message);
            if(enquiry != null) {
                System.out.println("Enquiry submitted successfully (ID: " + enquiry.getEnquiryId() + ")");
                return enquiry;
            } else {
                System.out.println("Failed to submit enquiry."); // Service should give reason
                return null;
            }
        } catch (NoSuchElementException | SecurityException e) {
            System.err.println("Enquiry Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while submitting enquiry: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles viewing all enquiries made by the applicant.
     * @param applicant The logged-in Applicant.
     * @return List of Enquiry objects, or empty list on error/no enquiries.
     */
    public List<Enquiry> handleViewMyEnquiries(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant context is missing.");
            return Collections.emptyList();
        }
        try {
            List<Enquiry> enquiries = applicantService.viewMyEnquiries(applicant);
            if (enquiries.isEmpty()) {
                System.out.println("You have not submitted any enquiries.");
            }
            // View layer handles display
            return enquiries;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching your enquiries: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles editing an existing enquiry.
     * @param applicant   The logged-in Applicant (for permission check).
     * @param enquiryId   The ID of the enquiry to edit.
     * @param newMessage  The new message content.
     * @return true if successful, false otherwise.
     */
    public boolean handleEditEnquiry(Applicant applicant, String enquiryId, String newMessage) {
        if (applicant == null || enquiryId == null || newMessage == null || newMessage.trim().isEmpty()) {
            System.out.println("Error: Missing applicant, enquiry ID, or new message.");
            return false;
        }
        try {
            // Fetch the enquiry first to pass the object to the service (or modify service to take ID)
            Enquiry enquiry = applicantService.viewMyEnquiryById(enquiryId, applicant); // Checks ownership implicitly
            if (enquiry == null) {
                // viewMyEnquiryById throws SecurityException if not owned, or returns null if not found.
                // If null is returned, it means not found for this user.
                System.out.println("Enquiry with ID " + enquiryId + " not found or you don't have permission to edit it.");
                return false;
            }
            if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()){
                System.out.println("Cannot edit an enquiry that has already been replied to.");
                return false;
            }


            boolean success = applicantService.editEnquiry(enquiry, newMessage, applicant);
            if (success) {
                System.out.println("Enquiry " + enquiryId + " updated successfully.");
                return true;
            } else {
                System.out.println("Failed to edit enquiry."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | SecurityException e) {
            System.err.println("Edit Enquiry Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while editing enquiry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles deleting an existing enquiry.
     * @param applicant The logged-in Applicant (for permission check).
     * @param enquiryId The ID of the enquiry to delete.
     * @return true if successful, false otherwise.
     */
    public boolean handleDeleteEnquiry(Applicant applicant, String enquiryId) {
        if (applicant == null || enquiryId == null) {
            System.out.println("Error: Missing applicant or enquiry ID.");
            return false;
        }
        try {
            // Optional: Confirm deletion in the View layer first.
            // Fetch the enquiry to check if it exists and can be deleted (e.g., not replied to?)
            Enquiry enquiry = applicantService.viewMyEnquiryById(enquiryId, applicant); // Checks ownership implicitly
            if (enquiry == null) {
                System.out.println("Enquiry with ID " + enquiryId + " not found or you don't have permission to delete it.");
                return false;
            }
            if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()){
                System.out.println("Cannot delete an enquiry that has already been replied to.");
                return false;
            }

            boolean success = applicantService.deleteEnquiry(enquiryId, applicant);
            if (success) {
                System.out.println("Enquiry " + enquiryId + " deleted successfully.");
                return true;
            } else {
                System.out.println("Failed to delete enquiry."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | SecurityException e) {
            System.err.println("Delete Enquiry Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while deleting enquiry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles viewing a specific project by ID, considering applicant eligibility.
     * @param applicant The currently logged-in Applicant.
     * @param projectId The ID of the project to view.
     * @return The Project object or null if not found/accessible.
     */
    public Project handleViewProjectById(Applicant applicant, String projectId) {
        if (applicant == null || projectId == null) {
            System.out.println("Error: Missing applicant or project ID.");
            return null;
        }
        try {
            Project project = applicantService.viewApplicantProjectById(projectId, applicant);
            if (project == null) {
                System.out.println("Project with ID " + projectId + " not found or not accessible to you.");
            }
            // View layer handles display
            return project;
        } catch (SecurityException e) {
            System.err.println("Access Denied: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching project details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles filtering available projects based on criteria.
     * @param applicant The currently logged-in Applicant.
     * @param filters   A Map where keys are filter criteria (e.g., "neighbourhood") and values are the filter values.
     * @return List of filtered Project objects.
     */
    public List<Project> handleFilterAvailableProjects(Applicant applicant, Map<String, String> filters) {
        if (applicant == null || filters == null) {
            System.out.println("Error: Missing applicant context or filter criteria.");
            return Collections.emptyList();
        }
        try {
            List<Project> projects = applicantService.filterApplicantProjects(filters, applicant);
            if (projects.isEmpty()) {
                System.out.println("No projects found matching your filter criteria and eligibility.");
            }
            // View layer handles display
            return projects;
        } catch (Exception e) {
            System.err.println("An error occurred while filtering projects: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}