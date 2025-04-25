package view;

import controller.*;
import entity.*;
import pub_enums.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Provides a Command Line Interface (CLI) for interacting with the BTO Management System.
 * Handles user input, displays menus and results, and calls controller methods.
 */
public class CLIView {

    private final Scanner scanner;
    private final UserController userController;
    private final ApplicantController applicantController;
    private final HdbOfficerController officerController;
    private final HdbManagerController managerController;

    private User currentUser = null; // Track the logged-in user

    /**
     * Constructor for CLIView.
     * Injects the required controllers.
     * @param uc The UserController instance.
     * @param ac The ApplicantController instance.
     * @param hoc The HdbOfficerController instance.
     * @param hmc The HdbManagerController instance.
     */
    public CLIView(UserController uc, ApplicantController ac, HdbOfficerController hoc, HdbManagerController hmc) {
        this.scanner = new Scanner(System.in);
        this.userController = uc;
        this.applicantController = ac;
        this.officerController = hoc;
        this.managerController = hmc;
    }

    /**
     * Starts the main application loop, handling login attempts and routing to role-specific menus.
     */
    public void run() {
        System.out.println("============================================");
        System.out.println(" Welcome to the BTO Management System (CLI)");
        System.out.println("============================================");

        int loginAttempts = 0;
        int maxLoginAttempts = 3; // Limit for non-interactive environments
        
        while (currentUser == null && loginAttempts < maxLoginAttempts) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            if (choice == 1) {
                handleLoginAttempt();
                loginAttempts++; // Count login attempts
            } else if (choice == 2) {
                System.out.println("Exiting application. Goodbye!");
                scanner.close(); // Close scanner on exit
                return; // Exit loop and method
            } else {
                displayMessage("Invalid choice. Please try again.");
            }
        }
        
        // Exit after max attempts in non-interactive mode
        if (currentUser == null && loginAttempts >= maxLoginAttempts) {
            System.out.println("Maximum login attempts reached. Exiting application.");
            scanner.close();
            return;
        }

        // If login was successful, currentUser is not null, proceed to role menu
        if (currentUser != null) {
            routeToRoleMenu();
        }

        scanner.close(); // Ensure scanner is closed if loop exits unexpectedly
    }

    // --- Menu Display ---

    /**
     * Displays the main menu (Login/Exit).
     */
    private void displayMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.println("-----------------");
    }

    /**
     * Routes the logged-in user to their respective role-based menu.
     * Handles the logout process and restarts the main loop.
     */
    private void routeToRoleMenu() {
        if (currentUser == null) {
            displayMessage("Error: No user logged in.");
            return; // Should not happen if called correctly
        }

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\nLogged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
            switch (currentUser.getRole()) {
                case APPLICANT:
                    loggedIn = displayApplicantMenu();
                    break;
                case HDBOFFICER:
                    loggedIn = displayOfficerMenu();
                    break;
                case HDBMANAGER:
                    loggedIn = displayManagerMenu();
                    break;
                default:
                    displayMessage("Error: Unknown user role.");
                    loggedIn = false; // Logout on error
                    break;
            }
        }
        // After loop finishes (logout), reset currentUser and potentially loop back to main menu
        currentUser = null;
        System.out.println("\nYou have been logged out.");
        run(); // Restart the main loop to show login screen again
    }

    /**
     * Displays the menu options available to an Applicant user and handles their choice.
     *
     * @return false if the user chooses to logout, true otherwise (to continue showing the menu).
     */
    private boolean displayApplicantMenu() {
        System.out.println("\n--- Applicant Menu ---");
        System.out.println("1. View Available Projects");
        System.out.println("2. Filter/Search Projects");
        System.out.println("3. Apply for Project");
        System.out.println("4. View Application Status");
        System.out.println("5. Request Application Withdrawal");
        System.out.println("6. View My Enquiries");
        System.out.println("7. Submit New Enquiry");
        System.out.println("8. Edit My Enquiry");
        System.out.println("9. Delete My Enquiry");
        System.out.println("10. Change Password");
        System.out.println("11. Logout");
        System.out.println("----------------------");

        int choice = getIntInput("Enter your choice: ");
        Applicant applicant = (Applicant) currentUser; // Safe cast after role check

        switch (choice) {
            case 1:
                handleViewAvailableProjects(applicant);
                break;
            case 2:
                handleFilterApplicantProjects(applicant);
                break;
            case 3:
                handleApplyForProject(applicant);
                break;
            case 4:
                handleViewApplicationStatus(applicant);
                break;
            case 5:
                handleRequestWithdrawal(applicant);
                break;
            case 6:
                handleViewMyEnquiries(applicant);
                break;
            case 7:
                handleSubmitEnquiry(applicant);
                break;
            case 8:
                handleEditEnquiry(applicant);
                break;
            case 9:
                handleDeleteEnquiry(applicant);
                break;
            case 10:
                if (handleChangePassword()) { return false; } // logout after successful password change
                break;
            case 11:
                return false; // Signal logout
            default:
                displayMessage("Invalid choice. Please try again.");
                break;
        }
        return true; // Continue showing menu
    }


    /**
     * Displays the menu options available to an HDB Officer user and handles their choice.
     * Includes both Officer-specific and Applicant-specific functionalities.
     *
     * @return false if the user chooses to logout, true otherwise (to continue showing the menu).
     */
    private boolean displayOfficerMenu() {
        System.out.println("\n--- HDB Officer Menu ---");
        System.out.println("--- Officer Actions ---");
        System.out.println("1. View All Projects (Officer View)"); // Officer view might differ slightly
        System.out.println("2. Filter/Search Projects (Officer View)");
        System.out.println("3. View Assigned Projects");
        System.out.println("4. Register for Project");
        System.out.println("5. View Enquiries for Assigned Projects");
        System.out.println("6. Reply to Enquiry");
        System.out.println("7. Process Application (Approve/Reject)");
        System.out.println("8. Process Withdrawal Request");
        System.out.println("--- Applicant Actions ---");
        System.out.println("9. View Available Projects (as Applicant)");
        System.out.println("10. Filter/Search Projects (as Applicant)");
        System.out.println("11. Apply for Project (as Applicant)");
        System.out.println("12. View My Application Status");
        System.out.println("13. Request My Application Withdrawal");
        System.out.println("14. View My Enquiries");
        System.out.println("15. Submit New Enquiry");
        System.out.println("16. Edit My Enquiry");
        System.out.println("17. Delete My Enquiry");
        System.out.println("--- General Actions ---");
        System.out.println("18. Change Password");
        System.out.println("19. Logout");
        System.out.println("------------------------");

        int choice = getIntInput("Enter your choice: ");
        // Cast currentUser to HdbOfficer. Since HdbOfficer extends Applicant,
        // it can be used where an Applicant is expected.
        HdbOfficer officer = (HdbOfficer) currentUser;

        switch (choice) {
            // --- Officer Actions ---
            case 1:
                handleViewAllProjects(officer); // Uses officer's view logic
                break;
            case 2:
                handleFilterProjects(officer); // Uses officer's filter logic
                break;
            case 3:
                handleViewAssignedProjects(officer);
                break;
            case 4:
                handleRegisterForProject(officer);
                break;
            case 5:
                handleViewEnquiriesForOfficer(officer);
                break;
            case 6:
                handleReplyToEnquiry(officer);
                break;
            case 7:
                handleProcessApplication(officer);
                break;
            case 8:
                handleProcessWithdrawalRequest(officer);
                break;
            // --- Applicant Actions (using the HdbOfficer object as an Applicant) ---
            case 9:
                // Explicitly call the applicant version of view available projects
                handleViewAvailableProjects(officer);
                break;
            case 10:
                // Explicitly call the applicant version of filter projects
                handleFilterApplicantProjects(officer);
                break;
            case 11:
                handleApplyForProject(officer);
                break;
            case 12:
                handleViewApplicationStatus(officer);
                break;
            case 13:
                handleRequestWithdrawal(officer);
                break;
            case 14:
                handleViewMyEnquiries(officer);
                break;
            case 15:
                handleSubmitEnquiry(officer);
                break;
            case 16:
                handleEditEnquiry(officer);
                break;
            case 17:
                handleDeleteEnquiry(officer);
                break;
            // --- General Actions ---
            case 18:
                handleChangePassword();
                break;
            case 19:
                return false; // Signal logout
            default:
                displayMessage("Invalid choice. Please try again.");
                break;
        }
        return true; // Continue showing menu
    }

    /**
     * Displays the menu options available to an HDB Manager user and handles their choice.
     *
     * @return false if the user chooses to logout, true otherwise (to continue showing the menu).
     */
    private boolean displayManagerMenu() {
        System.out.println("\n--- HDB Manager Menu ---");
        System.out.println("1. View All Projects");
        System.out.println("2. View My Projects");
        System.out.println("3. Create New Project");
        System.out.println("4. Update Project");
        System.out.println("5. Delete Project");
        System.out.println("6. Assign Officer to Project");
        System.out.println("7. Process Application");
        System.out.println("8. Process Withdrawal Request");
        System.out.println("9. Generate Report");
        System.out.println("10. Change Password");
        System.out.println("11. Logout");
        System.out.println("--------------------------");

        int choice = getIntInput("Enter your choice: ");
        HdbManager manager = (HdbManager) currentUser; // Safe cast

        switch (choice) {
            case 1:
                handleViewAllProjects(manager);
                break;
            case 2:
                handleViewManagedProjects(manager);
                break;
            case 3:
                handleCreateProject(manager);
                break;
            case 4:
                handleUpdateProject(manager);
                break;
            case 5:
                handleDeleteProject(manager);
                break;
            case 6:
                handleAssignOfficer(manager);
                break;
            case 7:
                handleProcessApplicationManager(manager);
                break;
            case 8:
                handleProcessWithdrawalManager(manager);
                break;
            case 9:
                handleGenerateReport(manager);
                break;
            case 10:
                if (handleChangePassword()) { return false; } // logout after successful password change
                break;
            case 11:
                return false; // Signal logout
            default:
                displayMessage("Invalid choice. Please try again.");
                break;
        }
        return true; // Continue showing menu
    }

    // --- Input Handling ---

    /**
     * Handles the user login process, prompting for ID and password, and validating credentials.
     */
    private void handleLoginAttempt() {
        try {
            System.out.println("\n--- Login ---");
            String userId = getStringInput("Enter ID: ");
            checkValidIdFormat(userId); // Test case 2: NRIC format notification
            userId = userId.toUpperCase(); // in case they key in lowercase, we store in uppercase
            String password = getPasswordInput("Enter Password: ");
            currentUser = userController.login(userId, password);
        }
        catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    /**
     * Checks if the provided ID string matches the NRIC format.
     * @param id The ID string to validate.
     * @throws IllegalArgumentException if the ID format is invalid.
     */
    private void checkValidIdFormat(String id) {
        if (!Pattern.matches("^[sStTfFgGmM]\\d{7}[a-zA-Z]$", id)) {
            throw new IllegalArgumentException("Invalid NRIC format");
        }
    }

    /**
     * Handles the password change process for the current user.
     * Prompts for old and new passwords, confirms the new password, and calls the controller.
     * @return true if the password was successfully changed (triggers logout), false otherwise.
     */
    private boolean handleChangePassword() {
        System.out.println("\n--- Change Password ---");
        String oldPassword = getPasswordInput("Enter Current Password: ");
        String newPassword = getPasswordInput("Enter New Password: ");
        String confirmPassword = getPasswordInput("Confirm New Password: ");

        if (!newPassword.equals(confirmPassword)) {
            displayMessage("New passwords do not match. Please try again.");
        }
        else{
            boolean result = userController.changePassword(currentUser, oldPassword, newPassword);
            if (result) {
                System.out.println("Password successfully changed. Please re-login.");
                return true;
            }
        }
        return false;
    }

    // --- Applicant Handlers ---

    /**
     * Handles viewing available projects for an applicant.
     * @param applicant The applicant viewing the projects.
     */
    private void handleViewAvailableProjects(Applicant applicant) {
        System.out.println("\n--- Available Projects ---");
        List<Project> projects = applicantController.viewAvailableProjects(applicant);
        displayProjectsApplicant(projects);
    }

    /**
     * Handles filtering projects based on applicant's input criteria.
     * @param applicant The applicant filtering the projects.
     */
    private void handleFilterApplicantProjects(Applicant applicant) {
        System.out.println("\n--- Filter Projects ---");
        Map<String, String> filters = getProjectFilters();
        List<Project> projects = applicantController.filterProjects(filters, applicant);
        displayProjectsApplicant(projects);
    }

    /**
     * Handles the process of an applicant applying for a project.
     * @param applicant The applicant applying for the project.
     */
    private void handleApplyForProject(Applicant applicant) {
        System.out.println("\n--- Apply for Project ---");
        List<Project> projects = applicantController.viewAvailableProjects(applicant);
        if (projects.isEmpty()) {
            return;
        }
        
        displayProjectsApplicant(projects);
        Project selectedProject = getProjectInput(projects);
        
        if (selectedProject == null) {
            System.out.println("Invalid project selected. Please try again.");
            return;
        }
        
        if (getConfirmation("Confirm application for " + selectedProject.getProjName() + "? (Y/N): ")) {
            boolean result = applicantController.applyForProject(applicant, selectedProject);
        } else {
            System.out.println("Application cancelled.");
        }
    }

    /**
     * Handles viewing the application status for an applicant.
     * @param applicant The applicant checking their status.
     */
    private void handleViewApplicationStatus(Applicant applicant) {
        System.out.println("\n--- Application Status ---");
        Application application = applicantController.checkApplicationStatus(applicant);
        if (application != null) {
            displayApplication(application);
        }
    }

    /**
     * Handles the process of an applicant requesting to withdraw their application.
     * @param applicant The applicant requesting withdrawal.
     */
    private void handleRequestWithdrawal(Applicant applicant) {
        System.out.println("\n--- Request Withdrawal ---");
        Application application = applicantController.checkApplicationStatus(applicant);
        
        if (application == null) {
            System.out.println("You don't have any active applications to withdraw.");
            return;
        }
        
        if (application.getStatus() == ApplStatus.WITHDRAW_PENDING) {
            System.out.println("Your withdrawal request is already pending.");
            return;
        }
        
        if (application.getStatus() == ApplStatus.WITHDRAW_APPROVED) {
            System.out.println("Your application has already been withdrawn.");
            return;
        }
        
        if (getConfirmation("Are you sure you want to withdraw your application? (Y/N): ")) {
            boolean result = applicantController.requestWithdrawal(applicant);
            if (result) {
                System.out.println("Withdrawal request submitted successfully.");
            } else {
                System.out.println("Failed to submit withdrawal request.");
            }
        } else {
            System.out.println("Withdrawal request cancelled.");
        }
    }


    /**
     * Handles viewing enquiries submitted by the applicant.
     * @param applicant The applicant viewing their enquiries.
     */
    private void handleViewMyEnquiries(Applicant applicant) {
        System.out.println("\n--- My Enquiries ---");
        List<Enquiry> enquiries = applicantController.viewEnquiries(applicant);
        displayEnquiries(enquiries);
    }

    private Project getProjectInput(List<Project> projects) {
        int choice = getIntInput("Enter the project number to select (1-" + projects.size() + "):");
        String projectId = "";
        if (choice > 0 && choice <= projects.size()) {
//            projectId = projects.get(choice-1).getProjectId();
            return projects.get(choice-1);
        }
        return null;
    }

    /**
     * Handles the process of an applicant submitting a new enquiry about a project.
     * @param applicant The applicant submitting the enquiry.
     */
    private void handleSubmitEnquiry(Applicant applicant) {
        System.out.println("\n--- Submit Enquiry ---");
        List<Project> projects = applicantController.viewAvailableProjects(applicant);
        if (projects.isEmpty()) {
            return;
        }

        displayProjectsApplicant(projects);
        Project selectedProject = getProjectInput(projects);
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            return;
        }
        
        String message = getStringInput("Enter your enquiry message: ");
        if (message.trim().isEmpty()) {
            System.out.println("Message cannot be empty. Enquiry cancelled.");
            return;
        }
        
        Enquiry enquiry = applicantController.submitEnquiry(applicant, selectedProject, message);
    }

    private Enquiry getEnquiryInput(List<Enquiry> enquiries) {
        int choice = getIntInput("Enter the enquiry number to select (1-" + enquiries.size() + "):");
        if (choice > 0 && choice <= enquiries.size()) {
            return enquiries.get(choice-1);
        }
        return null;
    }

    /**
     * Handles the process of an applicant editing their previously submitted enquiry.
     * @param applicant The applicant editing the enquiry.
     */
    private void handleEditEnquiry(Applicant applicant) {
        System.out.println("\n--- Edit Enquiry ---");
        List<Enquiry> enquiries = applicantController.viewEnquiries(applicant);
        if (enquiries.isEmpty()) {
            return;
        }
        
        displayEnquiries(enquiries);
        Enquiry selectedEnquiry = getEnquiryInput(enquiries);
        
        if (selectedEnquiry == null) {
            System.out.println("Invalid enquiry ID. Please try again.");
            return;
        }
        
        if (selectedEnquiry.getReply() != null && !selectedEnquiry.getReply().isEmpty()) {
            System.out.println("Cannot edit an enquiry that has already been replied to.");
            return;
        }
        
        String newMessage = getStringInput("Enter new message: ");
        if (newMessage.trim().isEmpty()) {
            System.out.println("Message cannot be empty. Edit cancelled.");
            return;
        }
        
        boolean result = applicantController.editEnquiry(selectedEnquiry.getEnquiryId(), applicant, newMessage);
    }

    /**
     * Handles the process of an applicant deleting their previously submitted enquiry.
     * @param applicant The applicant deleting the enquiry.
     */
    private void handleDeleteEnquiry(Applicant applicant) {
        System.out.println("\n--- Delete Enquiry ---");
        List<Enquiry> enquiries = applicantController.viewEnquiries(applicant);
        if (enquiries.isEmpty()) {
            return;
        }
        
        displayEnquiries(enquiries);
        Enquiry selectedEnquiry = getEnquiryInput(enquiries);
        
        if (selectedEnquiry == null) {
            System.out.println("Invalid enquiry ID. Please try again.");
            return;
        }
        
        if (selectedEnquiry.getReply() != null && !selectedEnquiry.getReply().isEmpty()) {
            System.out.println("Cannot delete an enquiry that has already been replied to.");
            return;
        }
        
        if (getConfirmation("Are you sure you want to delete this enquiry? (Y/N): ")) {
            boolean result = applicantController.deleteEnquiry(selectedEnquiry.getEnquiryId(), applicant);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // --- Officer Handlers ---

    /**
     * Handles viewing all projects (potentially with different details for officers/managers).
     * @param user The HDB Officer or Manager viewing the projects.
     */
    private void handleViewAllProjects(User user) {
        System.out.println("\n--- All Projects ---");
        List<Project> projects;
        
        if (user instanceof HdbOfficer) {
            projects = officerController.filterProjects(Map.of(), (HdbOfficer) user);
        } else if (user instanceof HdbManager) {
            projects = managerController.viewAllProjects((HdbManager) user);
        } else {
            System.out.println("Error: Invalid user type.");
            return;
        }
        
        displayProjects(projects);
    }

    /**
     * Handles filtering projects based on officer's/manager's input criteria.
     * @param user The HDB Officer or Manager filtering the projects.
     */
    private void handleFilterProjects(User user) {
        System.out.println("\n--- Filter Projects ---");
        Map<String, String> filters = getProjectFilters();
        List<Project> projects;
        
        if (user instanceof HdbOfficer) {
            projects = officerController.filterProjects(filters, (HdbOfficer) user);
        } else if (user instanceof HdbManager) {
            projects = managerController.filterProjects(filters, (HdbManager) user);
        } else {
            System.out.println("Error: Invalid user type.");
            return;
        }
        
        displayProjects(projects);
    }


    /**
     * Handles viewing projects assigned to a specific HDB officer.
     * @param officer The HDB officer viewing their assigned projects.
     */
    private void handleViewAssignedProjects(HdbOfficer officer) {
        System.out.println("\n--- Assigned Projects ---");
        List<Project> projects = officerController.viewAssignedProjects(officer);
        displayProjects(projects);
    }

    /**
     * Handles the process of an HDB officer registering themselves for a project.
     * @param officer The HDB officer registering for the project.
     */
    private void handleRegisterForProject(HdbOfficer officer) {
        System.out.println("\n--- Register for Project ---");
        List<Project> projects = officerController.filterProjects(Map.of(), officer);
        if (projects.isEmpty()) {
            return;
        }
        
        displayProjects(projects);
        System.out.println("Enter the ID of the project you want to register for:");
        String projectId = getStringInput("Project ID: ");
        
        Project selectedProject = null;
        for (Project project : projects) {
            if (project.getProjectId().equals(projectId)) {
                selectedProject = project;
                break;
            }
        }

        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            return;
        }
        
        if (getConfirmation("Confirm registration for project " + selectedProject.getProjName() + "? (Y/N): ")) {
            boolean result = officerController.registerForProject(officer, selectedProject);
            if (result) {
                System.out.println("Registration submitted successfully.");
            } else {
                System.out.println("Failed to register for project.");
            }
        } else {
            System.out.println("Registration cancelled.");
        }
    }

    /**
     /**
     * Handles viewing enquiries related to projects assigned to the HDB officer.
     * @param officer The HDB officer viewing the enquiries.
     */
    private void handleViewEnquiriesForOfficer(HdbOfficer officer) {
        System.out.println("\n--- Enquiries for Assigned Projects ---");
        List<Enquiry> enquiries = officerController.viewEnquiries(officer);
        displayEnquiries(enquiries);
    }

    /**
     * Handles the process of an HDB officer replying to an enquiry.
     * @param officer The HDB officer replying to the enquiry.
     */
    private void handleReplyToEnquiry(HdbOfficer officer) {
        System.out.println("\n--- Reply to Enquiry ---");
        List<Enquiry> enquiries = officerController.viewEnquiries(officer);
        if (enquiries.isEmpty()) {
            return;
        }
        
        displayEnquiries(enquiries);
        System.out.println("Enter the ID of the enquiry you want to reply to:");
        String enquiryId = getStringInput("Enquiry ID: ");
        
        Enquiry selectedEnquiry = null;
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getEnquiryId().equals(enquiryId)) {
                selectedEnquiry = enquiry;
                break;
            }
        }
        
        if (selectedEnquiry == null) {
            System.out.println("Invalid enquiry ID. Please try again.");
            return;
        }
        
        if (selectedEnquiry.getReply() != null && !selectedEnquiry.getReply().isEmpty()) {
            System.out.println("This enquiry has already been replied to.");
            return;
        }
        
        String replyMessage = getStringInput("Enter your reply: ");
        if (replyMessage.trim().isEmpty()) {
            System.out.println("Reply cannot be empty. Reply cancelled.");
            return;
        }
        
        boolean result = officerController.replyToEnquiry(enquiryId, officer, replyMessage);
        if (result) {
            System.out.println("Reply submitted successfully.");
        } else {
            System.out.println("Failed to submit reply.");
        }
    }

    /**
     * Handles the process of an HDB officer approving or rejecting an application.
     * @param officer The HDB officer processing the application.
     */
    private void handleProcessApplication(HdbOfficer officer) {
        System.out.println("\n--- Process Application ---");
        String applicationId = getStringInput("Enter the ID of the application to process: ");
        
        if (applicationId.trim().isEmpty()) {
            System.out.println("Application ID cannot be empty. Processing cancelled.");
            return;
        }
        
        System.out.println("1. Approve");
        System.out.println("2. Reject");
        int choice = getIntInput("Enter your choice (1-2): ");
        
        boolean approve = choice == 1;
        
        if (getConfirmation("Confirm " + (approve ? "approval" : "rejection") + " of application " + applicationId + "? (Y/N): ")) {
            boolean result = officerController.processApplication(applicationId, officer, approve);
            if (result) {
                System.out.println("Application " + (approve ? "approved" : "rejected") + " successfully.");
            } else {
                System.out.println("Failed to process application.");
            }
        } else {
            System.out.println("Processing cancelled.");
        }
    }

    /**
     * Handles the process of an HDB officer approving or rejecting an application withdrawal request.
     * @param officer The HDB officer processing the withdrawal request.
     */
    private void handleProcessWithdrawalRequest(HdbOfficer officer) {
        System.out.println("\n--- Process Withdrawal Request ---");
        // This is a simplified version. In a real application, you would query for pending withdrawal requests
        String applicationId = getStringInput("Enter the ID of the application with withdrawal request: ");
        
        if (applicationId.trim().isEmpty()) {
            System.out.println("Application ID cannot be empty. Processing cancelled.");
            return;
        }
        
        System.out.println("1. Approve withdrawal");
        System.out.println("2. Reject withdrawal");
        int choice = getIntInput("Enter your choice (1-2): ");
        
        boolean approve = choice == 1;
        
        if (getConfirmation("Confirm " + (approve ? "approval" : "rejection") + " of withdrawal request for application " + applicationId + "? (Y/N): ")) {
            boolean result = officerController.processWithdrawalRequest(applicationId, officer, approve);
            if (result) {
                System.out.println("Withdrawal request " + (approve ? "approved" : "rejected") + " successfully.");
            } else {
                System.out.println("Failed to process withdrawal request.");
            }
        } else {
            System.out.println("Processing cancelled.");
        }
    }

    // --- Manager Handlers ---

    /**
     * Handles viewing projects managed by a specific HDB manager.
     * @param manager The HDB manager viewing their projects.
     */
    private void handleViewManagedProjects(HdbManager manager) {
        System.out.println("\n--- Managed Projects ---");
        List<Project> projects = managerController.viewManagedProjects(manager);
        displayProjects(projects);
    }

    /**
     * Handles the process of an HDB manager creating a new project.
     * @param manager The HDB manager creating the project.
     */
    private void handleCreateProject(HdbManager manager) {
        System.out.println("\n--- Create Project ---");
        Map<String, Object> projectDetails = new HashMap<>();
        
        projectDetails.put("projectName", getStringInput("Project Name: "));
        projectDetails.put("neighbourhood", getStringInput("Neighbourhood: "));
        Date applStartDate = getDateInput("Application Start Date (yyyy-MM-dd): ");

        // check if start date overlaps with current active project
        Project activeProject = getActiveProject(manager);
        if (activeProject != null) {
            if (checkProjectClash(activeProject.getAppClose(), applStartDate)) {
                System.out.println("Cancelling project creation...");
                return;
            }
        }

        projectDetails.put("startDate", applStartDate);
        projectDetails.put("endDate", getDateInput("Application End Date (yyyy-MM-dd): "));
        projectDetails.put("units2Room", getIntInput("Number of 2-Room Flats: "));
        projectDetails.put("price2Room", getDoubleInput("Price of 2-Room Flats: "));
        projectDetails.put("units3Room", getIntInput("Number of 3-Room Flats: "));
        projectDetails.put("price3Room", getDoubleInput("Price of 3-Room Flats: "));
        projectDetails.put("officerSlots", getIntInput("Number of Officer Slots: "));
        projectDetails.put("isVisible", getConfirmation("Make project visible? (Y/N): "));



        Project project = managerController.createProject(manager, projectDetails);
        if (project != null) {
            System.out.println("Project created successfully with ID: " + project.getProjectId());
        } else {
            System.out.println("Failed to create project.");
        }
    }

    /**
     * Checks if a new project's start date clashes with an existing active project's end date.
     * @param activeEndDate The end date of the currently active project.
     * @param newStartDate The start date of the new project.
     * @return true if there is a clash, false otherwise.
     */
    private boolean checkProjectClash(Date activeEndDate, Date newStartDate) {
        if (newStartDate.compareTo(activeEndDate) < 0) {
            System.out.println("Project application period clash detected");
            return true;
        }
        return false;
    }

    /**
     * Retrieves the currently active project managed by the manager, if any.
     * @param manager The HDB manager.
     * @return The active Project object, or null if no project is currently active.
     */
    private Project getActiveProject(HdbManager manager) {
        List<Project> projects = managerController.viewManagedProjects(manager);
        LocalDate today = LocalDate.now();
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (!projects.isEmpty()) {
            for (Project p : projects) {
                if (todayDate.compareTo(p.getAppOpen()) >= 0 && todayDate.compareTo(p.getAppClose()) <= 0) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Handles the process of an HDB manager updating an existing project's details.
     * @param manager The HDB manager updating the project.
     */
    private void handleUpdateProject(HdbManager manager) {
        System.out.println("\n--- Update Project ---");
        List<Project> projects = managerController.viewManagedProjects(manager);
        if (projects.isEmpty()) {
            return;
        }
        
        displayProjects(projects);
        System.out.println("Enter the ID of the project you want to update:");
        String projectId = getStringInput("Project ID: ");
        
        Project selectedProject = null;
        for (Project project : projects) {
            if (project.getProjectId().equals(projectId)) {
                selectedProject = project;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        System.out.println("Leave field empty to keep current value.");
        
        String input = getStringInput("New Project Name [" + selectedProject.getProjName() + "]: ");
        if (!input.trim().isEmpty()) {
            updates.put("projectName", input);
        }
        
        input = getStringInput("New Neighbourhood [" + selectedProject.getNeighbourhood() + "]: ");
        if (!input.trim().isEmpty()) {
            updates.put("neighbourhood", input);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false);
        try {
            input = getStringInput("Change application opening date [" + selectedProject.getAppOpen() + "] (use yyyy-MM-dd format): ");
            Date newStartDate = (Date) formatter.parse(input);
            Project activeProject = getActiveProject(manager);
            if (activeProject != null) {
                if (checkProjectClash(activeProject.getAppClose(), newStartDate)) {
                    System.out.println("Cancelling project update...");
                    return;
                }
            }
            if (newStartDate.compareTo(selectedProject.getAppClose()) < 0) {
                updates.put("startDate", newStartDate);
            } else {
                System.out.println("Application opening date must be before application closing date!");
            }
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd.");
        }

        try {
            input = getStringInput("Change application closing date [" + selectedProject.getAppClose() + "] (use yyyy-MM-dd format): ");
            Date newEndDate = (Date) formatter.parse(input);
            if (newEndDate.compareTo(selectedProject.getAppClose()) < 0) {
                updates.put("endDate", newEndDate);
            } else {
                System.out.println("Application closing date must be after application opening date!");
            }
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd.");
        }

        int new2RmUnits = getIntInput("Change number of 2-Room Flats: ");
        if (new2RmUnits >= 0) {
            updates.put("units2Room", new2RmUnits);
        }
        else {
            System.out.println("Number of flats must be zero or more.");
        }

        Double new2RmPrice = getDoubleInput("Change price of 2-Room Flats: ");
        if (new2RmPrice > 100000.0) {
            updates.put("price2Room", new2RmPrice);
        }
        else{
            System.out.println("Flat price must be more than $100,000.");
        }

        int new3RmUnits = getIntInput("Change number of 3-Room Flats: ");
        if (new3RmUnits >= 0) {
            updates.put("units3Room", new3RmUnits);
        }
        else {
            System.out.println("Number of flats must be zero or more.");
        }

        Double new3RmPrice = getDoubleInput("Change price of 3-Room Flats: ");
        if (new3RmPrice > 150000.0) {
            updates.put("price3Room", new3RmPrice);
        }
        else{
            System.out.println("Flat price must be more than $150,000.");
        }

        int newOfficerSlots = getIntInput("Change number of officer slots [" + selectedProject.getOfficerSlots() + "]: ");
        if (newOfficerSlots > 0) {
            updates.put("officerSlots", newOfficerSlots);
        }
        else {
            System.out.println("Number of officer slots must be more than zero.");
        }

        String newVisible = getStringInput("Make visible (true/false) [" + selectedProject.isVisible() + "]: ");
        if (!newVisible.trim().isEmpty()) {
            updates.put("isVisible", Boolean.parseBoolean(newVisible));
        }

        if (!updates.isEmpty() && getConfirmation("Confirm updates to project " + selectedProject.getProjName() + "? (Y/N): ")) {
            boolean result = managerController.updateProject(manager, selectedProject, updates);
            if (result) {
                System.out.println("Project updated successfully.");
            } else {
                System.out.println("Failed to update project.");
            }
        } else {
            System.out.println("Update cancelled.");
        }
    }

    /**
     * Handles the process of an HDB manager deleting an existing project.
     * @param manager The HDB manager deleting the project.
     */
    private void handleDeleteProject(HdbManager manager) {
        System.out.println("\n--- Delete Project ---");
        List<Project> projects = managerController.viewManagedProjects(manager);
        if (projects.isEmpty()) {
            return;
        }

        displayProjects(projects);
        System.out.println("Enter the ID of the project you want to delete:");
        String projectId = getStringInput("Project ID: ");

        Project selectedProject = null;
        for (Project project : projects) {
            if (project.getProjectId().equals(projectId)) {
                selectedProject = project;
                break;
            }
        }

        if (selectedProject == null) {
            System.out.println("Invalid project ID. Please try again.");
            return;
        }

        String confirm = getStringInput(String.format("Type \"CONFIRM\" to delete project %s: \n", selectedProject.getProjName()));
        if (!confirm.trim().isEmpty() && confirm.equalsIgnoreCase("CONFIRM")) {
            managerController.deleteProject(manager, selectedProject);
        }
        else {
            System.out.println("Cancelling project deletion...");
        }
    }

    /**
     * Handles the process of an HDB manager assigning an HDB officer to a project.
     * @param manager The HDB manager assigning the officer.
     */
    private void handleAssignOfficer(HdbManager manager) {
        System.out.println("\n--- Assign Officer to Project ---");
        String officerId = getStringInput("Enter Officer ID: ");
        String projectId = getStringInput("Enter Project ID: ");
        
        if (officerId.trim().isEmpty() || projectId.trim().isEmpty()) {
            System.out.println("Officer ID and Project ID cannot be empty. Assignment cancelled.");
            return;
        }
        
        if (getConfirmation("Confirm assignment of Officer " + officerId + " to Project " + projectId + "? (Y/N): ")) {
            boolean result = managerController.assignOfficer(manager, officerId, projectId);
            if (result) {
                System.out.println("Officer assigned successfully.");
            } else {
                System.out.println("Failed to assign officer.");
            }
        } else {
            System.out.println("Assignment cancelled.");
        }
    }

    /**
     * Handles the process of an HDB manager approving or rejecting an application.
     * @param manager The HDB manager processing the application.
     */
    private void handleProcessApplicationManager(HdbManager manager) {
        System.out.println("\n--- Process Application (Manager) ---");


        String applicationId = getStringInput("Enter the ID of the application to process: ");
        
        if (applicationId.trim().isEmpty()) {
            System.out.println("Application ID cannot be empty. Processing cancelled.");
            return;
        }
        
        System.out.println("1. Approve");
        System.out.println("2. Reject");
        int choice = getIntInput("Enter your choice (1-2): ");
        
        boolean approve = choice == 1;
        
        if (getConfirmation("Confirm " + (approve ? "approval" : "rejection") + " of application " + applicationId + "? (Y/N): ")) {
            boolean result = managerController.processApplication(applicationId, manager, approve);
            if (result) {
                System.out.println("Application " + (approve ? "approved" : "rejected") + " successfully.");
            } else {
                System.out.println("Failed to process application.");
            }
        } else {
            System.out.println("Processing cancelled.");
        }
    }

    /**
     * Handles the process of an HDB manager approving or rejecting an application withdrawal request.
     * @param manager The HDB manager processing the withdrawal request.
     */
    private void handleProcessWithdrawalManager(HdbManager manager) {
        System.out.println("\n--- Process Withdrawal Request (Manager) ---");
        String applicationId = getStringInput("Enter the ID of the application with withdrawal request: ");

        if (applicationId.trim().isEmpty()) {
            System.out.println("Application ID cannot be empty. Processing cancelled.");
            return;
        }
        
        System.out.println("1. Approve withdrawal");
        System.out.println("2. Reject withdrawal");
        int choice = getIntInput("Enter your choice (1-2): ");
        
        if (choice == 1) {
            if (getConfirmation("Confirm approval of withdrawal for application " + applicationId + "? (Y/N): ")) {
                boolean result = managerController.approveWithdrawal(manager, applicationId);
                if (result) {
                    System.out.println("Withdrawal approved successfully.");
                } else {
                    System.out.println("Failed to approve withdrawal.");
                }
            } else {
                System.out.println("Processing cancelled.");
            }
        } else if (choice == 2) {
            if (getConfirmation("Confirm rejection of withdrawal for application " + applicationId + "? (Y/N): ")) {
                boolean result = managerController.rejectWithdrawal(manager, applicationId);
                if (result) {
                    System.out.println("Withdrawal rejected successfully.");
                } else {
                    System.out.println("Failed to reject withdrawal.");
                }
            } else {
                System.out.println("Processing cancelled.");
            }
        } else {
            System.out.println("Invalid choice. Processing cancelled.");
        }
    }

    /**
     * Handles the generation of reports based on filter criteria provided by the manager.
     * @param manager The HDB manager generating the report.
     */
    private void handleGenerateReport(HdbManager manager) {
        System.out.println("\n--- Generate Report ---");
        Map<String, String> filters = new HashMap<>();
        
        System.out.println("Enter filter criteria (leave blank to ignore):");
        String input = getStringInput("Filter by Project ID: ");
        if (!input.trim().isEmpty()) {
            filters.put("projectId", input);
        }
        
        input = getStringInput("Filter by Status (e.g., APPROVED, BOOKED): ");
        if (!input.trim().isEmpty()) {
            filters.put("status", input);
        }
        
        System.out.println("Generating report...");
        List<Object> reportData = managerController.generateBookingReport(filters, manager);
        
        System.out.println("\n--- Report Results ---");
        System.out.println("Total entries: " + reportData.size());
        // In a real application, you would display the report data in a formatted way
    }

    // --- Helper Methods ---

    /**
     * Prompts the user for project filter criteria (neighbourhood, flat type, project name).
     * @return A map containing the filter keys and values.
     */
    private Map<String, String> getProjectFilters() {
        Map<String, String> filters = new HashMap<>();
        String input;
        
        input = getStringInput("Neighbourhood (or leave blank): ");
        if (!input.trim().isEmpty()) {
            filters.put("neighbourhood", input);
        }
        
        input = getStringInput("Flat Type (TWOROOM/THREEROOM or leave blank): ");
        if (!input.trim().isEmpty()) {
            filters.put("flatType", input);
        }
        
        input = getStringInput("Project Name (or leave blank): ");
        if (!input.trim().isEmpty()) {
            filters.put("projectName", input);
        }
        
        return filters;
    }

    /**
     * Displays a list of projects in a formatted table, tailored for applicant view (less detail).
     * @param projects The list of projects to display.
     */
    private void displayProjectsApplicant(List<Project> projects) {
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        System.out.println("\n--- Available Projects ---");
        System.out.println("=================================================================================================================================");
        System.out.printf("%-5s | %-20s | %-15s | %-10s | %-12s | %-10s | %-12s%n",
                "No.", "Name", "Neighbourhood", "2-Room", "2-Room Price", "3-Room", "3-Room Price");
        System.out.println("=================================================================================================================================");

        int projectNumber = 1;
        for (Project project : projects) {
            List<Flat> flatList = project.getFlats();
            String twoRoomAvail = flatList.getFirst().getTotal() > 0 ? "Available" : "Unavailable";
            double twoRoomPrice = flatList.getFirst().getPrice();
            String threeRoomAvail = flatList.getLast().getTotal() > 0 ? "Available" : "Unavailable";
            double threeRoomPrice = flatList.getLast().getPrice();

            System.out.printf("%-5d | %-20s | %-15s | %-10s | $%-11.0f | %-10s | $%-11.0f%n",
                    projectNumber++,
                    project.getProjName(),
                    project.getNeighbourhood(),
                    twoRoomAvail,
                    twoRoomPrice,
                    threeRoomAvail,
                    threeRoomPrice);
        }

        System.out.println("=================================================================================================================================");
    }

    /**
     * Displays a list of projects in a formatted table (more detailed view for staff).
     * @param projects The list of projects to display.
     */
    private void displayProjects(List<Project> projects) {
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }
        
        System.out.println("\n--- Projects ---");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-20s | %-15s | %-10s | %-12s | %-10s | %-12s | %-8s | %-12s%n",
                "ID", "Name", "Neighbourhood", "2-Room", "2-Room Price", "3-Room", "3-Room Price", "Visible", "Officer Slots");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        List<Flat> flatList = new ArrayList<>();
        for (Project project : projects) {
            flatList = project.getFlats();
            String twoRoomAvail = flatList.getFirst().getTotal() > 0 ? "Available" : "Unavailable";
            double twoRoomPrice = flatList.getFirst().getPrice();
            String threeRoomAvail = flatList.getLast().getTotal() > 0 ? "Available" : "Unavailable";
            double threeRoomPrice = flatList.getLast().getPrice();
            System.out.printf("%-10s | %-20s | %-15s | %-10s | $%-11.0f | %-10s | $%-11.0f | %-8s | %-12d%n",
                    project.getProjectId(),
                    project.getProjName(),
                    project.getNeighbourhood(),
                    twoRoomAvail,
                    twoRoomPrice,
                    threeRoomAvail,
                    threeRoomPrice,
                    project.isVisible() ? "Yes" : "No",
                    project.getOfficerSlots());
        }

        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Displays the details of a single application.
     * @param application The Application object to display.
     */
    private void displayApplication(Application application) {
        if (application == null) {
            System.out.println("No application found.");
            return;
        }
        
        System.out.println("\n--- Application Details ---");
        System.out.println("ID: " + application.getId());
        System.out.println("Applicant ID: " + application.getApplicantId());
        System.out.println("Project ID: " + application.getProjectId());
        System.out.println("Flat Type: " + application.getFlatType());
        System.out.println("Status: " + application.getStatus());
    }

    /**
     * Displays a list of enquiries in a formatted table.
     * @param enquiries The list of Enquiry objects to display.
     */
    private void displayEnquiries(List<Enquiry> enquiries) {
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries found.");
            return;
        }
        
        System.out.println("\n--- Enquiries ---");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-36s | %-15s | %-20s | %-30s | %-30s\n", "No.", "ID", "Applicant ID", "Project ID", "Message", "Reply");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        int counter = 1;
        for (Enquiry enquiry : enquiries) {
            System.out.printf("%-5d | %-36s | %-15s | %-20s | %-30s | %-30s\n",
                    counter++,
                    enquiry.getEnquiryId(),
                    enquiry.getApplicantId(),
                    enquiry.getProjectId(),
                    truncateString(enquiry.getMessage(), 30),
                    truncateString(enquiry.getReply(), 30));
        }

        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
    }
    /**
     * Truncates a string to a maximum length, adding ellipsis if truncated.
     * @param text The string to truncate.
     * @param maxLength The maximum allowed length.
     * @return The truncated string (or the original string if shorter than maxLength).
     */
    private String truncateString(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        
        if (text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Displays a generic message to the console.
     * @param message The message to display.
     */
    private void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Gets integer input from the user with error handling.
     * Includes handling for environments without standard input.
     * @param prompt The message to display before getting input.
     * @return The integer entered by the user, or a default value (1) if input fails.
     */
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                
                // In case we're running this in an environment where standard input isn't available
                // TO REMOVE?
                if (!scanner.hasNextLine()) {
                    System.out.println("No input available. Defaulting to option 1.");
                    return 1; // Default to option 1 (Login)
                }
                
                // Otherwise read the input
                String input = scanner.nextLine();
                if (input == null || input.trim().isEmpty()) {
                    System.out.println("Empty input. Please enter a number.");
                    continue;
                }
                
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            } catch (NoSuchElementException e) {
                System.out.println("Input not available. Defaulting to option 1.");
                return 1; // Default to option 1 (Login)
            } catch (Exception e) {
                System.out.println("Error reading input: " + e.getMessage());
                return 1; // Default to option 1 (Login)
            }
        }
    }

    /**
     * Gets double input from the user with error handling.
     * @param prompt The message to display before getting input.
     * @return The double entered by the user, or null if input fails.
     */
    private Double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                if (!scanner.hasNextLine()) {
                    return null;
                }

                // Read the input
                String input = scanner.nextLine();
                if (input == null || input.trim().isEmpty()) {
                    System.out.println("Empty input. Please enter a number.");
                    continue;
                }

                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            } catch (Exception e) {
                System.out.println("Error reading input: " + e.getMessage());
            }
        }
    }

    /**
     * Gets string input from the user with error handling.
     * Includes handling for environments without standard input.
     * @param prompt The message to display before getting input.
     * @return The string entered by the user, or an empty string if input fails.
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        try {
            if (!scanner.hasNextLine()) {
                System.out.println("No input available. Defaulting to empty string.");
                return ""; // Default to empty string
            }
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            System.out.println("Input not available. Defaulting to empty string.");
            return ""; // Default to empty string
        } catch (Exception e) {
            System.out.println("Error reading input: " + e.getMessage());
            return ""; // Default to empty string
        }
    }

    /**
     * Gets password input from the user (currently reads as plain text).
     * Includes handling for environments without standard input.
     * @param prompt The message to display before getting input.
     * @return The password string entered by the user, or a default value if input fails.
     */
    private String getPasswordInput(String prompt) {
        System.out.print(prompt);
        try {
            if (!scanner.hasNextLine()) {
                System.out.println("No input available. Defaulting to 'password'.");
                return "password"; // Default password for testing TO REMOVE
            }
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            System.out.println("Input not available. Defaulting to 'password'.");
            return "password"; // Default password for testing
        } catch (Exception e) {
            System.out.println("Error reading input: " + e.getMessage());
            return "password"; // Default password for testing
        }
    }

    /**
     * Gets a confirmation (Y/N) from the user with error handling.
     * Includes handling for environments without standard input.
     * @param prompt The confirmation question to display.
     * @return true if the user confirms (Y/Yes), false otherwise (N/No). Defaults to true on input error.
     */
    private boolean getConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("No input available. Defaulting to 'yes'.");
                    return true; // Default to yes
                }
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("y") || input.equals("yes")) {
                    return true;
                } else if (input.equals("n") || input.equals("no")) {
                    return false;
                } else {
                    System.out.println("Invalid input. Please enter 'Y' or 'N'.");
                }
            } catch (NoSuchElementException e) {
                System.out.println("Input not available. Defaulting to 'yes'.");
                return true; // Default to yes
            } catch (Exception e) {
                System.out.println("Error reading input: " + e.getMessage());
                return true; // Default to yes
            }
        }
    }

    /**
     * Gets date input from the user in yyyy-MM-dd format with validation.
     * Includes handling for environments without standard input.
     * @param prompt The message to display before getting input.
     * @return The Date object entered by the user, or today's date if input fails.
     */
    private Date getDateInput(String prompt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("No input available. Defaulting to today's date.");
                    return new Date(); // Default to today's date
                }
                String input = scanner.nextLine();
                try {
                    return sdf.parse(input);
                } catch (ParseException e) {
                    System.out.println("Invalid date format. Please use yyyy-MM-dd.");
                }
            } catch (NoSuchElementException e) {
                System.out.println("Input not available. Defaulting to today's date.");
                return new Date(); // Default to today's date
            } catch (Exception e) {
                System.out.println("Error reading input: " + e.getMessage());
                return new Date(); // Default to today's date
            }
        }
    }
}