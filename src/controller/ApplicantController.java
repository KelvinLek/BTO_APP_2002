package controller;

import entity.*;
import pub_enums.*;
import service.ApplicantService;
import service.UserService;

import java.util.*;

/**
 * Controller for Applicant-specific operations
 */
public class ApplicantController extends UserController {
    private ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService, UserService userService) {
        super(userService);
        this.applicantService = applicantService;
    }

    /**
     * View all available projects for an applicant
     * 
     * @param applicant The applicant viewing projects
     * @return List of available projects
     */
    public List<Project> viewAvailableProjects(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant information missing.");
            return Collections.emptyList();
        }
        
        List<Project> projects = applicantService.viewProjectsByUser(applicant);
        
        if (projects.isEmpty()) {
            System.out.println("No projects are currently available for you.");
        }
        
        return projects;
    }

    /**
     * View details of a specific project
     * 
     * @param projectId The ID of the project to view
     * @param applicant The applicant viewing the project
     * @return The project details or null if not found/not authorized
     */
    public Project viewProjectDetails(String projectId, Applicant applicant) {
        if (projectId == null || applicant == null) {
            System.out.println("Error: Project ID or applicant information missing.");
            return null;
        }
        
        Project project = applicantService.viewProjectById(projectId, applicant);
        
        if (project == null) {
            System.out.println("Project not found or not available for viewing.");
        }
        
        return project;
    }

    /**
     * Apply for a BTO project
     * 
     * @param applicant The applicant applying
     * @param project The project to apply for
     * @return true if application successful, false otherwise
     */
    public boolean applyForProject(Applicant applicant, Project project) {
        if (applicant == null || project == null) {
            System.out.println("Error: Applicant or project information missing.");
            return false;
        }
        
        // First check eligibility
        if (!applicantService.checkEligibility(applicant, project)) {
            System.out.println("You are not eligible to apply for this project.");
            return false;
        }
        
        boolean result = applicantService.applyForProject(applicant, project);
        
        if (result) {
            System.out.println("Application submitted successfully.");
        } else {
            System.out.println("Failed to submit application. Please try again.");
        }
        
        return result;
    }

    /**
     * Check current application status
     * 
     * @param applicant The applicant checking status
     * @return The current application or null if none exists
     */
    public Application checkApplicationStatus(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant information missing.");
            return null;
        }
        
        Application application = applicantService.getApplicationStatus(applicant);
        
        if (application == null) {
            System.out.println("You don't have any active applications.");
        } else {
            System.out.println("Your application status: " + application.getStatus());
        }
        
        return application;
    }

    /**
     * Request withdrawal of an application
     * 
     * @param applicant The applicant requesting withdrawal
     * @return true if request successful, false otherwise
     */
    public boolean requestWithdrawal(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant information missing.");
            return false;
        }
        
        Application application = applicantService.getApplicationStatus(applicant);
        
        if (application == null) {
            System.out.println("You don't have any active applications to withdraw.");
            return false;
        }
        
        if (application.getStatus() == ApplStatus.WITHDRAW_PENDING) {
            System.out.println("Your withdrawal request is already pending.");
            return false;
        }
        
        if (application.getStatus() == ApplStatus.WITHDRAW_APPROVED) {
            System.out.println("Your application has already been withdrawn.");
            return false;
        }
        
        boolean result = applicantService.requestWithdrawal(application);
        
        if (result) {
            System.out.println("Withdrawal request submitted successfully.");
        } else {
            System.out.println("Failed to submit withdrawal request. Please try again.");
        }
        
        return result;
    }

    /**
     * Submit an enquiry about a project
     * 
     * @param applicant The applicant submitting the enquiry
     * @param project The project the enquiry is about
     * @param message The enquiry message
     * @return The created enquiry or null if submission failed
     */
    public Enquiry submitEnquiry(Applicant applicant, Project project, String message) {
        if (applicant == null || project == null || message == null || message.trim().isEmpty()) {
            System.out.println("Error: Applicant, project, or message information missing.");
            return null;
        }
        
        Enquiry enquiry = applicantService.submitEnquiry(applicant, project, message);
        
        if (enquiry != null) {
            System.out.println("Enquiry submitted successfully.");
        } else {
            System.out.println("Failed to submit enquiry. Please try again.");
        }
        
        return enquiry;
    }

    /**
     * View all enquiries submitted by the applicant
     * 
     * @param applicant The applicant viewing enquiries
     * @return List of enquiries
     */
    public List<Enquiry> viewEnquiries(Applicant applicant) {
        if (applicant == null) {
            System.out.println("Error: Applicant information missing.");
            return Collections.emptyList();
        }
        
        List<Enquiry> enquiries = applicantService.viewEnquiries(applicant);
        
        if (enquiries.isEmpty()) {
            System.out.println("You haven't submitted any enquiries yet.");
        }
        
        return enquiries;
    }

    /**
     * View a specific enquiry
     * 
     * @param enquiryId The ID of the enquiry to view
     * @param applicant The applicant viewing the enquiry
     * @return The enquiry or null if not found/not authorized
     */
    public Enquiry viewEnquiry(String enquiryId, Applicant applicant) {
        if (enquiryId == null || applicant == null) {
            System.out.println("Error: Enquiry ID or applicant information missing.");
            return null;
        }
        
        Enquiry enquiry = applicantService.viewEnquiryById(enquiryId, applicant);
        
        if (enquiry == null) {
            System.out.println("Enquiry not found or not accessible.");
        }
        
        return enquiry;
    }

    /**
     * Edit an existing enquiry
     * 
     * @param enquiryId The ID of the enquiry to edit
     * @param applicant The applicant editing the enquiry
     * @param newMessage The new message
     * @return true if edit successful, false otherwise
     */
    public boolean editEnquiry(String enquiryId, Applicant applicant, String newMessage) {
        if (enquiryId == null || applicant == null || newMessage == null || newMessage.trim().isEmpty()) {
            System.out.println("Error: Enquiry ID, applicant, or message information missing.");
            return false;
        }
        
        // Check if enquiry exists and belongs to the applicant
        Enquiry enquiry = applicantService.viewEnquiryById(enquiryId, applicant);
        if (enquiry == null) {
            System.out.println("Enquiry not found or not accessible.");
            return false;
        }
        
        // Check if enquiry has already been replied to
        if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()) {
            System.out.println("Cannot edit an enquiry that has already been replied to.");
            return false;
        }
        
        applicantService.editEnquiry(enquiryId, applicant, newMessage);
        System.out.println("Enquiry updated successfully.");
        return true;
    }

    /**
     * Delete an enquiry
     * 
     * @param enquiryId The ID of the enquiry to delete
     * @param applicant The applicant deleting the enquiry
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteEnquiry(String enquiryId, Applicant applicant) {
        if (enquiryId == null || applicant == null) {
            System.out.println("Error: Enquiry ID or applicant information missing.");
            return false;
        }
        
        // Check if enquiry exists and belongs to the applicant
        Enquiry enquiry = applicantService.viewEnquiryById(enquiryId, applicant);
        if (enquiry == null) {
            System.out.println("Enquiry not found or not accessible.");
            return false;
        }
        
        // Check if enquiry has already been replied to
        if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()) {
            System.out.println("Cannot delete an enquiry that has already been replied to.");
            return false;
        }
        
        applicantService.deleteEnquiry(enquiryId, applicant);
        System.out.println("Enquiry deleted successfully.");
        return true;
    }

    /**
     * Filter projects based on criteria
     * 
     * @param filters Map of filter criteria
     * @param applicant The applicant filtering projects
     * @return List of filtered projects
     */
    public List<Project> filterProjects(Map<String, String> filters, Applicant applicant) {
        if (filters == null || applicant == null) {
            System.out.println("Error: Filter criteria or applicant information missing.");
            return Collections.emptyList();
        }
        
        List<Project> projects = applicantService.filterAllProjects(filters, applicant);
        
        if (projects.isEmpty()) {
            System.out.println("No projects match your filter criteria.");
        }
        
        return projects;
    }
}