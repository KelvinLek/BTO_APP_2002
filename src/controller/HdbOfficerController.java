package controller;

import entity.*;
import pub_enums.*;
import service.HdbOfficerService;
import service.UserService;

import java.util.List;
import java.util.Map;

/**
 * Controller for HDB Officer-specific operations
 */
public class HdbOfficerController extends UserController {
    private HdbOfficerService officerService;

    public HdbOfficerController(HdbOfficerService officerService, UserService userService) {
        super(userService);
        this.officerService = officerService;
    }

    /**
     * View assigned projects for an officer
     * 
     * @param officer The officer viewing projects
     * @return List of assigned projects
     */
    public List<Project> viewAssignedProjects(HdbOfficer officer) {
        if (officer == null) {
            System.out.println("Error: Officer information missing.");
            return List.of();
        }
        
        List<Project> projects = officerService.viewProjectsByUser(officer);
        
        if (projects.isEmpty()) {
            System.out.println("You are not assigned to any projects.");
        }
        
        return projects;
    }

    /**
     * View details of a specific project
     * 
     * @param projectId The ID of the project to view
     * @param officer The officer viewing the project
     * @return The project details or null if not found/not authorized
     */
    public Project viewProjectDetails(String projectId, HdbOfficer officer) {
        if (projectId == null || officer == null) {
            System.out.println("Error: Project ID or officer information missing.");
            return null;
        }
        
        Project project = officerService.viewProjectById(projectId, officer);
        
        if (project == null) {
            System.out.println("Project not found or not available for viewing.");
        }
        
        return project;
    }

    /**
     * View enquiries for projects assigned to the officer
     * 
     * @param officer The officer viewing enquiries
     * @return List of enquiries
     */
    public List<Enquiry> viewEnquiries(HdbOfficer officer) {
        if (officer == null) {
            System.out.println("Error: Officer information missing.");
            return List.of();
        }
        
        List<Enquiry> enquiries = officerService.viewEnquiries(officer);
        
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries found for your assigned projects.");
        }
        
        return enquiries;
    }

    /**
     * View a specific enquiry
     * 
     * @param enquiryId The ID of the enquiry to view
     * @param officer The officer viewing the enquiry
     * @return The enquiry or null if not found/not authorized
     */
    public Enquiry viewEnquiry(String enquiryId, HdbOfficer officer) {
        if (enquiryId == null || officer == null) {
            System.out.println("Error: Enquiry ID or officer information missing.");
            return null;
        }
        
        Enquiry enquiry = officerService.viewEnquiryById(enquiryId, officer);
        
        if (enquiry == null) {
            System.out.println("Enquiry not found or not accessible.");
        }
        
        return enquiry;
    }

    /**
     * Reply to an enquiry
     * 
     * @param enquiryId The ID of the enquiry to reply to
     * @param officer The officer replying
     * @param replyMessage The reply message
     * @return true if reply successful, false otherwise
     */
    public boolean replyToEnquiry(String enquiryId, HdbOfficer officer, String replyMessage) {
        if (enquiryId == null || officer == null || replyMessage == null || replyMessage.trim().isEmpty()) {
            System.out.println("Error: Enquiry ID, officer, or reply message information missing.");
            return false;
        }
        
        // Check if enquiry exists and belongs to an assigned project
        Enquiry enquiry = officerService.viewEnquiryById(enquiryId, officer);
        if (enquiry == null) {
            System.out.println("Enquiry not found or not accessible.");
            return false;
        }
        
        // Check if enquiry has already been replied to
        if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()) {
            System.out.println("This enquiry has already been replied to.");
            return false;
        }
        
        boolean result = officerService.replyToEnquiry(enquiryId, officer, replyMessage);
        
        if (result) {
            System.out.println("Reply submitted successfully.");
        } else {
            System.out.println("Failed to submit reply. Please try again.");
        }
        
        return result;
    }

    /**
     * Process an application (approve or reject)
     * 
     * @param applicationId The ID of the application to process
     * @param officer The officer processing the application
     * @param approve true to approve, false to reject
     * @return true if processing successful, false otherwise
     */
    public boolean processApplication(String applicationId, HdbOfficer officer, boolean approve) {
        if (applicationId == null || officer == null) {
            System.out.println("Error: Application ID or officer information missing.");
            return false;
        }
        
        boolean result = officerService.processApplication(applicationId, officer, approve);
        
        if (result) {
            System.out.println("Application " + (approve ? "approved" : "rejected") + " successfully.");
        } else {
            System.out.println("Failed to process application. Please try again.");
        }
        
        return result;
    }

    /**
     * Process a withdrawal request
     * 
     * @param applicationId The ID of the application with withdrawal request
     * @param officer The officer processing the request
     * @param approve true to approve withdrawal, false to reject the request
     * @return true if processing successful, false otherwise
     */
    public boolean processWithdrawalRequest(String applicationId, HdbOfficer officer, boolean approve) {
        if (applicationId == null || officer == null) {
            System.out.println("Error: Application ID or officer information missing.");
            return false;
        }
        
        boolean result = officerService.processWithdrawalRequest(applicationId, officer, approve);
        
        if (result) {
            System.out.println("Withdrawal request " + (approve ? "approved" : "rejected") + " successfully.");
        } else {
            System.out.println("Failed to process withdrawal request. Please try again.");
        }
        
        return result;
    }

    /**
     * Register for a project
     * 
     * @param officer The officer registering
     * @param project The project to register for
     * @return true if registration successful, false otherwise
     */
    public boolean registerForProject(HdbOfficer officer, Project project) {
        if (officer == null || project == null) {
            System.out.println("Error: Officer or project information missing.");
            return false;
        }
        
        boolean result = officerService.registerForProject(officer, project);
        
        if (result) {
            System.out.println("Registration for project successful.");
        } else {
            System.out.println("Failed to register for project. Please try again.");
        }
        
        return result;
    }
    
    /**
     * Filter projects based on criteria
     * 
     * @param filters Map of filter criteria
     * @param officer The officer filtering projects
     * @return List of filtered projects
     */
    public List<Project> filterProjects(Map<String, String> filters, HdbOfficer officer) {
        if (filters == null || officer == null) {
            System.out.println("Error: Filter criteria or officer information missing.");
            return List.of();
        }
        
        List<Project> projects = officerService.filterAllProjects(filters, officer);
        
        if (projects.isEmpty()) {
            System.out.println("No projects match your filter criteria.");
        }
        
        return projects;
    }

    /**
     * Check if applicant is eligible for a project
     * 
     * @param applicant The applicant to check
     * @param project The project to check eligibility for
     * @return true if eligible, false otherwise
     */
    public boolean checkApplicantEligibility(Applicant applicant, Project project) {
        if (applicant == null || project == null) {
            System.out.println("Error: Applicant or project information missing.");
            return false;
        }
        
        boolean eligible = officerService.checkEligibility(applicant, project);
        
        System.out.println("Applicant is " + (eligible ? "eligible" : "not eligible") + " for this project.");
        
        return eligible;
    }
}