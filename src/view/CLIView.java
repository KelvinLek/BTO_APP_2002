package view;

import controller.*;
import entity.*;
import pub_enums.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
     */
    public CLIView(UserController uc, ApplicantController ac, HdbOfficerController hoc, HdbManagerController hmc) {
        this.scanner = new Scanner(System.in);
        this.userController = uc;
        this.applicantController = ac;
        this.officerController = hoc;
        this.managerController = hmc;
    }

    /**
     * Starts the main application loop.
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

    private void displayMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.println("-----------------");
    }

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


    private boolean displayOfficerMenu() {
        System.out.println("\n--- HDB Officer Menu ---");
        System.out.println("1. View Available Projects");
        System.out.println("2. Filter/Search Projects");
        System.out.println("3. View Assigned Projects");
        System.out.println("4. Register for Project");
        System.out.println("5. View Enquiries for Assigned Projects");
        System.out.println("6. Reply to Enquiry");
        System.out.println("7. Process Application (Approve/Reject)");
        System.out.println("8. Process Withdrawal Request");
        System.out.println("9. Change Password");
        System.out.println("10. Logout");
        System.out.println("------------------------");

        int choice = getIntInput("Enter your choice: ");
        HdbOfficer officer = (HdbOfficer) currentUser; // Safe cast

        switch (choice) {
            case 1:
                handleViewAllProjects(officer);
                break;
            case 2:
                handleFilterProjects(officer);
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
            case 9:
                if (handleChangePassword()) { return false; } // logout after successful password change
                break;
            case 10:
                return false; // Signal logout
            default:
                displayMessage("Invalid choice. Please try again.");
                break;
        }
        return true; // Continue showing menu
    }

    private boolean displayManagerMenu() {
        System.out.println("\n--- HDB Manager Menu ---");
        System.out.println("1. View All Projects");
        System.out.println("2. View My Projects");
        System.out.println("3. Create New Project");
        System.out.println("4. Update Project");
        System.out.println("5. Assign Officer to Project");
        System.out.println("6. Process Application");
        System.out.println("7. Process Withdrawal Request");
        System.out.println("8. Generate Report");
        System.out.println("9. Change Password");
        System.out.println("10. Logout");
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
                handleAssignOfficer(manager);
                break;
            case 6:
                handleProcessApplicationManager(manager);
                break;
            case 7:
                handleProcessWithdrawalManager(manager);
                break;
            case 8:
                handleGenerateReport(manager);
                break;
            case 9:
                if (handleChangePassword()) { return false; } // logout after successful password change
                break;
            case 10:
                return false; // Signal logout
            default:
                displayMessage("Invalid choice. Please try again.");
                break;
        }
        return true; // Continue showing menu
    }

    // --- Input Handling ---

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

    private void checkValidIdFormat(String id) {
        if (!Pattern.matches("^[sStTfFgGmM]\\d{7}[a-zA-Z]$", id)) {
            throw new IllegalArgumentException("Invalid NRIC format");
        }
    }

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

    private void handleViewAvailableProjects(Applicant applicant) {
        System.out.println("\n--- Available Projects ---");
        List<Project> projects = applicantController.viewAvailableProjects(applicant);
        displayProjects(projects);
    }

    private void handleFilterApplicantProjects(Applicant applicant) {
        System.out.println("\n--- Filter Projects ---");
        Map<String, String> filters = getProjectFilters();
        List<Project> projects = applicantController.filterProjects(filters, applicant);
        displayProjects(projects);
    }

    private void handleApplyForProject(Applicant applicant) {
        System.out.println("\n--- Apply for Project ---");
        List<Project> projects = applicantController.viewAvailableProjects(applicant);
        if (projects.isEmpty()) {
            return;
        }
        
        displayProjects(projects);
        System.out.println("Enter the ID of the project you want to apply for:");
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
        
        if (getConfirmation("Confirm application for " + selectedProject.getProjName() + "? (Y/N): ")) {
            boolean result = applicantController.applyForProject(applicant, selectedProject);
            if (result) {
                System.out.println("Application submitted successfully.");
            } else {
                System.out.println("Application submission failed.");
            }
        } else {
            System.out.println("Application cancelled.");
        }
    }

    private void handleViewApplicationStatus(Applicant applicant) {
        System.out.println("\n--- Application Status ---");
        Application application = applicantController.checkApplicationStatus(applicant);
        if (application != null) {
            displayApplication(application);
        }
    }

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

    private void handleViewMyEnquiries(Applicant applicant) {
        System.out.println("\n--- My Enquiries ---");
        List<Enquiry> enquiries = applicantController.viewEnquiries(applicant);
        displayEnquiries(enquiries);
    }

    private void handleSubmitEnquiry(Applicant applicant) {
        System.out.println("\n--- Submit Enquiry ---");
        List<Project> projects = applicantController.viewAvailableProjects(applicant);
        if (projects.isEmpty()) {
            return;
        }
        
        displayProjects(projects);
        System.out.println("Enter the ID of the project you want to enquire about:");
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
        
        String message = getStringInput("Enter your enquiry message: ");
        if (message.trim().isEmpty()) {
            System.out.println("Message cannot be empty. Enquiry cancelled.");
            return;
        }
        
        Enquiry enquiry = applicantController.submitEnquiry(applicant, selectedProject, message);
        if (enquiry != null) {
            System.out.println("Enquiry submitted successfully.");
        } else {
            System.out.println("Failed to submit enquiry.");
        }
    }

    private void handleEditEnquiry(Applicant applicant) {
        System.out.println("\n--- Edit Enquiry ---");
        List<Enquiry> enquiries = applicantController.viewEnquiries(applicant);
        if (enquiries.isEmpty()) {
            return;
        }
        
        displayEnquiries(enquiries);
        System.out.println("Enter the ID of the enquiry you want to edit:");
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
            System.out.println("Cannot edit an enquiry that has already been replied to.");
            return;
        }
        
        String newMessage = getStringInput("Enter new message: ");
        if (newMessage.trim().isEmpty()) {
            System.out.println("Message cannot be empty. Edit cancelled.");
            return;
        }
        
        boolean result = applicantController.editEnquiry(enquiryId, applicant, newMessage);
        if (result) {
            System.out.println("Enquiry updated successfully.");
        } else {
            System.out.println("Failed to update enquiry.");
        }
    }

    private void handleDeleteEnquiry(Applicant applicant) {
        System.out.println("\n--- Delete Enquiry ---");
        List<Enquiry> enquiries = applicantController.viewEnquiries(applicant);
        if (enquiries.isEmpty()) {
            return;
        }
        
        displayEnquiries(enquiries);
        System.out.println("Enter the ID of the enquiry you want to delete:");
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
            System.out.println("Cannot delete an enquiry that has already been replied to.");
            return;
        }
        
        if (getConfirmation("Are you sure you want to delete this enquiry? (Y/N): ")) {
            boolean result = applicantController.deleteEnquiry(enquiryId, applicant);
            if (result) {
                System.out.println("Enquiry deleted successfully.");
            } else {
                System.out.println("Failed to delete enquiry.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // --- Officer Handlers ---

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

    private void handleViewAssignedProjects(HdbOfficer officer) {
        System.out.println("\n--- Assigned Projects ---");
        List<Project> projects = officerController.viewAssignedProjects(officer);
        displayProjects(projects);
    }

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

    private void handleViewEnquiriesForOfficer(HdbOfficer officer) {
        System.out.println("\n--- Enquiries for Assigned Projects ---");
        List<Enquiry> enquiries = officerController.viewEnquiries(officer);
        displayEnquiries(enquiries);
    }

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

    private void handleProcessApplication(HdbOfficer officer) {
        System.out.println("\n--- Process Application ---");
        // This is a simplified version. In a real application, you would query for pending applications
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

    private void handleViewManagedProjects(HdbManager manager) {
        System.out.println("\n--- Managed Projects ---");
        List<Project> projects = managerController.viewManagedProjects(manager);
        displayProjects(projects);
    }

    private void handleCreateProject(HdbManager manager) {
        System.out.println("\n--- Create Project ---");
        Map<String, Object> projectDetails = new HashMap<>();
        
        projectDetails.put("projectName", getStringInput("Project Name: "));
        projectDetails.put("neighbourhood", getStringInput("Neighbourhood: "));
        projectDetails.put("startDate", getDateInput("Application Start Date (yyyy-MM-dd): "));
        projectDetails.put("endDate", getDateInput("Application End Date (yyyy-MM-dd): "));
        projectDetails.put("units2Room", getIntInput("Number of 2-Room Flats: "));
        projectDetails.put("units3Room", getIntInput("Number of 3-Room Flats: "));
        projectDetails.put("officerSlots", getIntInput("Number of Officer Slots: "));
        projectDetails.put("isVisible", getConfirmation("Make project visible? (Y/N): "));
        
        Project project = managerController.createProject(manager, projectDetails);
        if (project != null) {
            System.out.println("Project created successfully with ID: " + project.getProjectId());
        } else {
            System.out.println("Failed to create project.");
        }
    }

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

    private void displayProjects(List<Project> projects) {
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }
        
        System.out.println("\n--- Projects ---");
        System.out.println("-----------------------------------------------------------------------------------------------------------");
        System.out.printf("%-20s | %-30s | %-15s | %-10s | %-10s\n", "ID", "Name", "Neighbourhood", "Visible", "Officer Slots");
        System.out.println("-----------------------------------------------------------------------------------------------------------");
        
        for (Project project : projects) {
            System.out.printf("%-20s | %-30s | %-15s | %-10s | %-10d\n",
                    project.getProjectId(),
                    project.getProjName(),
                    project.getNeighbourhood(),
                    project.isVisible() ? "Yes" : "No",
                    project.getOfficerSlots());
        }
        
        System.out.println("-----------------------------------------------------------------------------------------------------------");
    }

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

    private void displayEnquiries(List<Enquiry> enquiries) {
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries found.");
            return;
        }
        
        System.out.println("\n--- Enquiries ---");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.printf("%-20s | %-15s | %-20s | %-30s | %-30s\n", "ID", "Applicant ID", "Project ID", "Message", "Reply");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        
        for (Enquiry enquiry : enquiries) {
            System.out.printf("%-20s | %-15s | %-20s | %-30s | %-30s\n",
                    enquiry.getEnquiryId(),
                    enquiry.getApplicantId(),
                    enquiry.getProjectId(),
                    truncateString(enquiry.getMessage(), 30),
                    truncateString(enquiry.getReply(), 30));
        }
        
        System.out.println("----------------------------------------------------------------------------------------------------------");
    }

    private String truncateString(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        
        if (text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength - 3) + "...";
    }

    private void displayMessage(String message) {
        System.out.println(message);
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                
                // In case we're running this in an environment where standard input isn't available
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

    private String getPasswordInput(String prompt) {
        System.out.print(prompt);
        try {
            if (!scanner.hasNextLine()) {
                System.out.println("No input available. Defaulting to 'password'.");
                return "password"; // Default password for testing
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