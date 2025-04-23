package controller;

import entity.*;
import pub_enums.*;
import service.*;
import java.util.*;

/**
 * Controller for HDB Officer actions. Extends ApplicantController.
 * Requires an HdbOfficerService instance.
 */
public class HdbOfficerController extends ApplicantController {

    public final HdbOfficerService officerService; // Changed to public for access in view layer

    /**
     * Constructor for HdbOfficerController.
     * @param officerService   Service for officer operations.
     * @param applicantService Service for applicant operations (passed to super).
     * @param authService      Authentication service (passed to super).
     * @param passwordService  Password service (passed to super).
     */
    public HdbOfficerController(HdbOfficerService officerService, ApplicantService applicantService, IAuthService authService, IPasswordService passwordService) {
        super(applicantService, authService, passwordService); // Initialize base controllers
        this.officerService = officerService;
    }

    /**
     * Handles registering the officer for a specific project.
     * @param officer The currently logged-in HdbOfficer.
     * @param project The Project to register for.
     * @return true if registration request submitted, false otherwise.
     */
    public boolean handleRegisterForProject(HdbOfficer officer, Project project) {
        if (officer == null || project == null) {
            System.out.println("Error: Missing officer or project information.");
            return false;
        }
        try {
            boolean success = officerService.registerForProject(officer, project);
            if(success) {
                System.out.println("Registration request submitted for project: " + project.getProjName());
                return true;
            } else {
                System.out.println("Registration request failed."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | IllegalStateException | SecurityException e) {
            System.err.println("Registration Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles viewing the registration status for a specific project.
     * @param officer The currently logged-in HdbOfficer.
     * @param project The Project to check status for.
     */
    public void handleViewRegistrationStatus(HdbOfficer officer, Project project) {
        if (officer == null || project == null) {
            System.out.println("Error: Missing officer or project information.");
            return;
        }
        try {
            String status = officerService.viewRegistrationStatus(officer, project);
            System.out.println("Your registration status for project '" + project.getProjName() + "': " + status);
        } catch (NoSuchElementException e) {
            System.err.println("Error checking status: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while checking registration status: " + e.getMessage());
        }
    }

    /**
     * Handles booking a flat for a successful applicant.
     * @param officer     The currently logged-in HdbOfficer performing the booking.
     * @param application The Application (must be in SUCCESS status).
     * @param flatType    The FlatType being booked (should match application).
     * @return true if booking successful, false otherwise.
     */
    public boolean handleBookFlat(HdbOfficer officer, Application application, FlatType flatType) {
        if (officer == null || application == null || flatType == null) {
            System.out.println("Error: Missing officer, application, or flat type for booking.");
            return false;
        }
        // Basic check: Ensure the flat type matches the application
        if (!application.getFlatType().equalsIgnoreCase(flatType.name())) {
            System.out.println("Error: The selected flat type (" + flatType + ") does not match the application (" + application.getFlatType() + ").");
            return false;
        }

        try {
            boolean success = officerService.bookFlat(application, flatType, officer);
            if(success) {
                System.out.println("Flat booked successfully for applicant " + application.getApplicantId() + " (Application ID: " + application.getId() + ")");
                // Optionally trigger receipt generation display here via View
                return true;
            } else {
                System.out.println("Flat booking failed."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | IllegalStateException | SecurityException e) {
            System.err.println("Booking Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during flat booking: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles viewing projects assigned to the officer.
     * @param officer The currently logged-in HdbOfficer.
     * @return List of assigned Project objects.
     */
    public List<Project> handleViewMyAssignedProjects(HdbOfficer officer) {
        if (officer == null) {
            System.out.println("Error: Officer context is missing.");
            return Collections.emptyList();
        }
        try {
            List<Project> projects = officerService.viewMyAssignedProjects(officer);
            if (projects.isEmpty()) {
                System.out.println("You are not currently assigned to any projects.");
            }
            // View layer handles display
            return projects;
        } catch (Exception e) {
            System.err.println("An error occurred while fetching assigned projects: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Handles viewing enquiries for a specific assigned project.
     * @param officer   The currently logged-in HdbOfficer.
     * @param projectId The ID of the assigned project.
     * @return List of Enquiry objects for that project.
     */
    public List<Enquiry> handleViewEnquiriesByAssignedProject(HdbOfficer officer, String projectId) {
        if (officer == null || projectId == null) {
            System.out.println("Error: Missing officer or project ID.");
            return Collections.emptyList();
        }
        try {
            // The service method viewEnquiriesByAssignedProject includes the security check
            List<Enquiry> enquiries = officerService.viewEnquiriesByAssignedProject(projectId, officer);
            if (enquiries.isEmpty()) {
                System.out.println("No enquiries found for project ID: " + projectId);
            }
            // View layer handles display
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
     * Handles replying to a specific enquiry.
     * @param officer   The currently logged-in HdbOfficer.
     * @param enquiryId The ID of the enquiry to reply to.
     * @param replyText The text of the reply.
     * @return true if reply successful, false otherwise.
     */
    public boolean handleReplyToEnquiry(HdbOfficer officer, String enquiryId, String replyText) {
        if (officer == null || enquiryId == null || replyText == null || replyText.trim().isEmpty()) {
            System.out.println("Error: Missing officer, enquiry ID, or reply text.");
            return false;
        }
        try {
            // Fetch enquiry to pass the object to the service (or modify service)
            // Use the officer-specific view method to ensure they can access it first
            Enquiry enquiry = officerService.viewAssignedEnquiryById(enquiryId, officer);
            if (enquiry == null) {
                System.out.println("Enquiry with ID " + enquiryId + " not found or not accessible to you.");
                return false;
            }
            if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()) {
                System.out.println("This enquiry has already been replied to.");
                // Ask if overwrite? View layer decision. For now, prevent.
                return false;
            }

            boolean success = officerService.replyToEnquiry(enquiry, replyText, officer);
            if(success) {
                System.out.println("Reply submitted successfully for enquiry ID: " + enquiryId);
                return true;
            } else {
                System.out.println("Failed to submit reply."); // Service should give reason
                return false;
            }
        } catch (NoSuchElementException | SecurityException e) {
            System.err.println("Reply Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while submitting reply: " + e.getMessage());
            return false;
        }
    }
}