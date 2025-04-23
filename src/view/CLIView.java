package view;

import controller.*; // Import all controllers
import entity.*; // Import all entities
import pub_enums.*; // Import all enums

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*; // Scanner, List, Map, Date etc.

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
    public CLIView(UserController uc, ApplicantController ac, HdbOfficerController hoc, HdbManagerController mc) {
        this.scanner = new Scanner(System.in);
        this.userController = uc;
        this.applicantController = ac;
        this.officerController = hoc;
        this.managerController = mc;
    }

    /**
     * Starts the main application loop.
     */
    public void run() {
        System.out.println("============================================");
        System.out.println(" Welcome to the BTO Management System (CLI)");
        System.out.println("============================================");

        while (currentUser == null) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            if (choice == 1) {
                handleLoginAttempt();
            } else if (choice == 2) {
                System.out.println("Exiting application. Goodbye!");
                scanner.close(); // Close scanner on exit
                return; // Exit loop and method
            } else {
                displayMessage("Invalid choice. Please try again.");
            }
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
                    // Officers have Applicant permissions plus their own
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
                handleChangePassword();
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
        // Inherited Applicant options
        System.out.println("1. View Available Projects");
        System.out.println("2. Filter/Search Projects");
        System.out.println("3. View Application Status (Own)"); // Officer might apply too
        // Enquiries
        System.out.println("4. View My Enquiries");
        System.out.println("5. Submit New Enquiry (Own)");
        System.out.println("6. Edit My Enquiry");
        System.out.println("7. Delete My Enquiry");
        System.out.println("--- Officer Actions ---");
        System.out.println("8. Register for Project Duty");
        System.out.println("9. View Project Registration Status");
        System.out.println("10. View Assigned Projects");
        System.out.println("11. View Enquiries for Assigned Project");
        System.out.println("12. Reply to Enquiry");
        System.out.println("13. Book Flat for Applicant");
        System.out.println("--- General ---");
        System.out.println("14. Change Password");
        System.out.println("15. Logout");
        System.out.println("------------------------");

        int choice = getIntInput("Enter your choice: ");
        HdbOfficer officer = (HdbOfficer) currentUser; // Safe cast

        switch (choice) {
            // Applicant Actions (delegated to ApplicantController methods)
            case 1:
                handleViewAvailableProjects(officer); // Use officer object, behaves like applicant
                break;
            case 2:
                handleFilterApplicantProjects(officer); // Use officer object
                break;
            case 3:
                handleViewApplicationStatus(officer); // Use officer object
                break;
            case 4:
                handleViewMyEnquiries(officer); // Use officer object
                break;
            case 5:
                handleSubmitEnquiry(officer); // Use officer object
                break;
            case 6:
                handleEditEnquiry(officer); // Use officer object
                break;
            case 7:
                handleDeleteEnquiry(officer); // Use officer object
                break;
            // Officer Actions
            case 8:
                handleRegisterForProject(officer);
                break;
            case 9:
                handleViewProjectRegistrationStatus(officer);
                break;
            case 10:
                handleViewAssignedProjects(officer);
                break;
            case 11:
                handleViewEnquiriesForAssignedProject(officer);
                break;
            case 12:
                handleReplyToEnquiry(officer);
                break;
            case 13:
                handleBookFlat(officer);
                break;
            // General
            case 14:
                handleChangePassword();
                break;
            case 15:
                return false; // Signal logout
            default:
                displayMessage("Invalid choice. Please try again.");
                break;
        }
        return true; // Continue showing menu
    }

    private boolean displayManagerMenu() {
        System.out.println("\n--- HDB Manager Menu ---");
        System.out.println("--- Project Management ---");
        System.out.println("1. View All Projects");
        System.out.println("2. Filter/Search All Projects");
        System.out.println("3. View Project Details");
        System.out.println("4. Create New Project");
        System.out.println("5. Edit Project");
        System.out.println("6. Delete Project");
        System.out.println("7. Toggle Project Visibility");
        System.out.println("--- Approval Management ---");
        System.out.println("8. Process Officer Registration");
        System.out.println("9. Process Application Approval/Rejection");
        System.out.println("10. Process Application Withdrawal Approval/Rejection");
        System.out.println("--- Enquiry & Reporting ---");
        System.out.println("11. View All Enquiries");
        System.out.println("12. View Enquiries by Project");
        System.out.println("13. Reply to Any Enquiry");
        System.out.println("14. Generate Booking Report");
        System.out.println("--- General ---");
        System.out.println("15. Change Password");
        System.out.println("16. Logout");
        System.out.println("--------------------------");

        int choice = getIntInput("Enter your choice: ");
        HdbManager manager = (HdbManager) currentUser; // Safe cast

        switch (choice) {
            case 1: handleManagerViewAllProjects(manager); break;
            case 2: handleManagerFilterAllProjects(manager); break;
            case 3: handleManagerViewProjectDetails(manager); break;
            case 4: handleCreateProject(manager); break;
            case 5: handleEditProject(manager); break;
            case 6: handleDeleteProject(manager); break;
            case 7: handleToggleProjectVisibility(manager); break;
            case 8: handleProcessOfficerRegistration(manager); break;
            case 9: handleProcessApplicationApproval(manager); break;
            case 10: handleProcessWithdrawal(manager); break;
            case 11: handleManagerViewAllEnquiries(manager); break;
            case 12: handleManagerViewEnquiriesByProject(manager); break;
            case 13: handleManagerReplyToEnquiry(manager); break;
            case 14: handleGenerateBookingReport(manager); break;
            case 15: handleChangePassword(); break;
            case 16: return false; // Signal logout
            default: displayMessage("Invalid choice. Please try again."); break;
        }
        return true; // Continue showing menu
    }

    // --- Input Handling ---

    private void handleLoginAttempt() {
        System.out.println("\n--- Login ---");
        String nric = getStringInput("Enter NRIC: ");
        // Basic NRIC format validation (can be enhanced)
        if (!nric.matches("^[STst]\\d{7}[A-Za-z]$")) {
            displayMessage("Invalid NRIC format. Please try again.");
            return;
        }
        String password = getPasswordInput("Enter Password: "); // Simple password input
        currentUser = userController.handleLogin(nric, password);
        // routeToRoleMenu() will be called if login is successful in the main run loop
    }

    private void handleChangePassword() {
        if (currentUser == null) {
            displayMessage("Error: You must be logged in to change password.");
            return;
        }
        System.out.println("\n--- Change Password ---");
        String currentPassword = getPasswordInput("Enter Current Password: ");
        // Verify current password first (optional but recommended)
        // For simplicity, we assume the user enters it correctly and proceed.
        // A better implementation would re-authenticate or verify the current password via the service.

        String newPassword = getPasswordInput("Enter New Password: ");
        String confirmPassword = getPasswordInput("Confirm New Password: ");

        if (!newPassword.equals(confirmPassword)) {
            displayMessage("New passwords do not match. Please try again.");
            return;
        }
        // Check against current password (requires service support or storing it temporarily)
        // if(newPassword.equals(currentPassword)) { // Using the input, not secure
        //     displayMessage("New password cannot be the same as the old password.");
        //     return;
        // }

        boolean success = userController.handleChangePassword(currentUser, newPassword);
        if (success) {
            // Password changed successfully, message printed by controller.
        } else {
            // Failure message printed by controller or service.
        }
    }

    // --- Applicant Action Handlers ---

    private void handleViewAvailableProjects(Applicant applicant) {
        System.out.println("\n--- Available BTO Projects ---");
        List<Project> projects = applicantController.handleViewAvailableProjects(applicant);
        displayProjects(projects);
    }

    private void handleFilterApplicantProjects(Applicant applicant){
        System.out.println("\n--- Filter Projects ---");
        Map<String, String> filters = new HashMap<>();
        String input;

        input = getStringInput("Enter Neighbourhood (or leave blank): ");
        if(!input.isEmpty()) filters.put("neighbourhood", input);

        input = getStringInput("Enter Flat Type (TWOROOM/THREEROOM or leave blank): ");
        if(!input.isEmpty()) filters.put("flattype", input);

        input = getStringInput("Enter Project Name contains (or leave blank): ");
        if(!input.isEmpty()) filters.put("projectname", input);

        List<Project> projects = applicantController.handleFilterAvailableProjects(applicant, filters);
        displayProjects(projects);
    }


    private void handleApplyForProject(Applicant applicant) {
        System.out.println("\n--- Apply for Project ---");
        List<Project> projects = applicantController.handleViewAvailableProjects(applicant);
        if (projects.isEmpty()) {
            // Message already displayed by controller handler
            return;
        }
        displayProjects(projects);
        Project selectedProject = selectProjectFromList(projects);
        if (selectedProject == null) return;

        // Display available flat types for the selected project
        System.out.println("Available Flat Types for " + selectedProject.getProjName() + ":");
        List<FlatType> availableTypes = new ArrayList<>();
        if (selectedProject.getFlats() != null) {
            int typeIndex = 1;
            for (Flat flat : selectedProject.getFlats()) {
                // Check eligibility for this specific type
                if (applicantController.applicantService.checkEligibility(applicant, flat.getFlatType())) {
                    System.out.println(typeIndex + ". " + flat.getFlatType() + " (Remaining: " + flat.getRemaining() + ")");
                    availableTypes.add(flat.getFlatType());
                    typeIndex++;
                } else {
                    System.out.println("   " + flat.getFlatType() + " (Not Eligible)");
                }
            }
        }

        if (availableTypes.isEmpty()) {
            System.out.println("You are not eligible for any flat types in this project.");
            return;
        }


        int typeChoice = getIntInput("Select Flat Type (number): ");
        if (typeChoice < 1 || typeChoice > availableTypes.size()) {
            System.out.println("Invalid flat type selection.");
            return;
        }
        FlatType selectedFlatType = availableTypes.get(typeChoice - 1);

        if (getConfirmation("Confirm application for " + selectedProject.getProjName() + " (" + selectedFlatType + ")? (Y/N): ")) {
            applicantController.handleApplyForProject(applicant, selectedProject, selectedFlatType);
        } else {
            System.out.println("Application cancelled.");
        }
    }

    private void handleViewApplicationStatus(Applicant applicant) {
        System.out.println("\n--- Application Status ---");
        Application app = applicantController.handleGetApplicationStatus(applicant);
        displayApplication(app); // Handles null case inside
    }


    private void handleRequestWithdrawal(Applicant applicant) {
        System.out.println("\n--- Request Withdrawal ---");
        // Controller handles fetching app and confirmation logic now
        applicantController.handleRequestWithdrawal(applicant);
    }


    private void handleViewMyEnquiries(Applicant applicant) {
        System.out.println("\n--- Your Enquiries ---");
        List<Enquiry> enquiries = applicantController.handleViewMyEnquiries(applicant);
        displayEnquiries(enquiries);
    }

    private void handleSubmitEnquiry(Applicant applicant) {
        System.out.println("\n--- Submit New Enquiry ---");
        List<Project> projects = applicantController.handleViewAvailableProjects(applicant); // Show projects they can enquire about
        if (projects.isEmpty()) {
            System.out.println("No projects available to enquire about.");
            return;
        }
        displayProjects(projects);
        Project selectedProject = selectProjectFromList(projects);
        if (selectedProject == null) return;

        String message = getStringInput("Enter your enquiry message: ");
        if (!message.trim().isEmpty()) {
            if (getConfirmation("Submit this enquiry for project '" + selectedProject.getProjName() + "'? (Y/N): ")) {
                applicantController.handleSubmitEnquiry(applicant, selectedProject, message);
            } else {
                System.out.println("Enquiry submission cancelled.");
            }
        } else {
            System.out.println("Enquiry message cannot be empty.");
        }
    }

    private void handleEditEnquiry(Applicant applicant) {
        System.out.println("\n--- Edit Enquiry ---");
        List<Enquiry> enquiries = applicantController.handleViewMyEnquiries(applicant);
        if (enquiries.isEmpty()) {
            System.out.println("You have no enquiries to edit.");
            return;
        }
        // Filter out enquiries that have replies
        List<Enquiry> editableEnquiries = new ArrayList<>();
        for(Enquiry e : enquiries){
            if(e.getReply() == null || e.getReply().isEmpty()){
                editableEnquiries.add(e);
            }
        }

        if (editableEnquiries.isEmpty()) {
            System.out.println("You have no enquiries that can be edited (all have replies).");
            return;
        }


        System.out.println("Select an enquiry to edit:");
        displayEnquiries(editableEnquiries); // Show only editable ones

        Enquiry selectedEnquiry = selectEnquiryFromList(editableEnquiries);
        if (selectedEnquiry == null) return;

        String newMessage = getStringInput("Enter the new enquiry message: ");
        if (!newMessage.trim().isEmpty()) {
            if (getConfirmation("Confirm update to enquiry ID " + selectedEnquiry.getEnquiryId() + "? (Y/N): ")) {
                applicantController.handleEditEnquiry(applicant, selectedEnquiry.getEnquiryId(), newMessage);
            } else {
                System.out.println("Edit cancelled.");
            }
        } else {
            System.out.println("Enquiry message cannot be empty.");
        }
    }

    private void handleDeleteEnquiry(Applicant applicant) {
        System.out.println("\n--- Delete Enquiry ---");
        List<Enquiry> enquiries = applicantController.handleViewMyEnquiries(applicant);
        if (enquiries.isEmpty()) {
            System.out.println("You have no enquiries to delete.");
            return;
        }
        // Filter out enquiries that have replies
        List<Enquiry> deletableEnquiries = new ArrayList<>();
        for(Enquiry e : enquiries){
            if(e.getReply() == null || e.getReply().isEmpty()){
                deletableEnquiries.add(e);
            }
        }

        if (deletableEnquiries.isEmpty()) {
            System.out.println("You have no enquiries that can be deleted (all have replies).");
            return;
        }

        System.out.println("Select an enquiry to delete:");
        displayEnquiries(deletableEnquiries);

        Enquiry selectedEnquiry = selectEnquiryFromList(deletableEnquiries);
        if (selectedEnquiry == null) return;

        if (getConfirmation("Confirm deletion of enquiry ID " + selectedEnquiry.getEnquiryId() + "? This cannot be undone. (Y/N): ")) {
            applicantController.handleDeleteEnquiry(applicant, selectedEnquiry.getEnquiryId());
        } else {
            System.out.println("Deletion cancelled.");
        }
    }


    // --- Officer Action Handlers ---

    private void handleRegisterForProject(HdbOfficer officer) {
        System.out.println("\n--- Register for Project Duty ---");
        // Manager needs to list projects open for officer registration (visibility maybe not required?)
        // For now, just show all projects from manager's view
        List<Project> allProjects = managerController.handleViewAllProjects(null); // Get all projects
        if (allProjects.isEmpty()) return;

        List<Project> openProjects = new ArrayList<>();
        Date today = new Date();
        for(Project p : allProjects){
            // Check if project application period is valid (or use different dates for duty period?)
            if(p.getAppOpen() != null && p.getAppClose() != null /* && !today.after(p.getAppClose()) */){ // Allow register even if closed?
                // Check if officer already registered/assigned?
                String status = officerController.officerService.viewRegistrationStatus(officer, p);
                if("Not Registered".equals(status)) { // Only show if not registered
                    openProjects.add(p);
                }
            }
        }

        if(openProjects.isEmpty()){
            System.out.println("No projects currently available for registration or you are already registered/assigned.");
            return;
        }

        System.out.println("Select a project to register for:");
        displayProjects(openProjects);
        Project selectedProject = selectProjectFromList(openProjects);
        if (selectedProject == null) return;

        if (getConfirmation("Confirm registration request for project '" + selectedProject.getProjName() + "'? (Y/N): ")) {
            officerController.handleRegisterForProject(officer, selectedProject);
        } else {
            System.out.println("Registration cancelled.");
        }
    }

    private void handleViewProjectRegistrationStatus(HdbOfficer officer) {
        System.out.println("\n--- View Project Registration Status ---");
        // Show all projects the officer MIGHT be registered for (pending/approved/rejected)
        List<Project> allProjects = managerController.handleViewAllProjects(null); // Need all projects
        if (allProjects.isEmpty()) return;

        System.out.println("Registration Status for Projects:");
        boolean foundStatus = false;
        for(Project p : allProjects){
            try {
                String status = officerController.officerService.viewRegistrationStatus(officer, p);
                if(!"Not Registered".equals(status)) { // Only show if there's a status
                    System.out.printf("  - %-20s (ID: %s): %s%n", p.getProjName(), p.getProjectId(), status);
                    foundStatus = true;
                }
            } catch (Exception e) { /* Ignore projects officer can't access? */ }
        }
        if(!foundStatus){
            System.out.println("You have no pending or active project registrations.");
        }
    }

    private void handleViewAssignedProjects(HdbOfficer officer) {
        System.out.println("\n--- Assigned Projects ---");
        List<Project> projects = officerController.handleViewMyAssignedProjects(officer);
        displayProjects(projects);
    }

    private void handleViewEnquiriesForAssignedProject(HdbOfficer officer) {
        System.out.println("\n--- View Enquiries for Assigned Project ---");
        List<Project> assignedProjects = officerController.handleViewMyAssignedProjects(officer);
        if (assignedProjects.isEmpty()) {
            System.out.println("You are not assigned to any projects.");
            return;
        }
        System.out.println("Select an assigned project to view enquiries:");
        displayProjects(assignedProjects);
        Project selectedProject = selectProjectFromList(assignedProjects);
        if (selectedProject == null) return;

        List<Enquiry> enquiries = officerController.handleViewEnquiriesByAssignedProject(officer, selectedProject.getProjectId());
        displayEnquiries(enquiries);
    }

    private void handleReplyToEnquiry(HdbOfficer officer) {
        System.out.println("\n--- Reply to Enquiry ---");
        // Option 1: Show all enquiries for all assigned projects
        List<Enquiry> allUnrepliedEnquiries = new ArrayList<>();
        List<Project> assignedProjects = officerController.handleViewMyAssignedProjects(officer);
        if (assignedProjects.isEmpty()) { System.out.println("You are not assigned to any projects."); return; }

        for(Project p : assignedProjects){
            try {
                List<Enquiry> projectEnquiries = officerController.handleViewEnquiriesByAssignedProject(officer, p.getProjectId());
                for(Enquiry e : projectEnquiries){
                    if(e.getReply() == null || e.getReply().isEmpty()){
                        allUnrepliedEnquiries.add(e);
                    }
                }
            } catch (Exception e) { /* Ignore errors fetching for specific project */ }
        }


        if (allUnrepliedEnquiries.isEmpty()) {
            System.out.println("No enquiries requiring a reply found for your assigned projects.");
            return;
        }

        System.out.println("Select an enquiry to reply to:");
        displayEnquiries(allUnrepliedEnquiries); // Show project ID with enquiry
        Enquiry selectedEnquiry = selectEnquiryFromList(allUnrepliedEnquiries);
        if (selectedEnquiry == null) return;

        String replyText = getStringInput("Enter your reply: ");
        if (!replyText.trim().isEmpty()) {
            if (getConfirmation("Submit this reply for enquiry ID " + selectedEnquiry.getEnquiryId() + "? (Y/N): ")) {
                officerController.handleReplyToEnquiry(officer, selectedEnquiry.getEnquiryId(), replyText);
            } else {
                System.out.println("Reply cancelled.");
            }
        } else {
            System.out.println("Reply text cannot be empty.");
        }
    }

    private void handleBookFlat(HdbOfficer officer) {
        System.out.println("\n--- Book Flat for Applicant ---");
        // Need to find successful applications for projects the officer is assigned to
        List<Application> bookableApps = new ArrayList<>();
        List<Project> assignedProjects = officerController.handleViewMyAssignedProjects(officer);
        if (assignedProjects.isEmpty()) { System.out.println("You are not assigned to any projects to perform bookings."); return; }

        // This is inefficient without proper repository queries. Simulate by getting all success apps.
        List<Application> successApps = managerController.managerService.findApplicationsByStatusPlaceholder(ApplStatus.SUCCESS); // Need repo access

        if (successApps == null || successApps.isEmpty()){ System.out.println("No successful applications found system-wide."); return; }

        for(Application app : successApps) {
            // Check if the app's project is one the officer is assigned to
            boolean projectAssigned = false;
            for(Project p : assignedProjects) {
                if(p.getProjectId().equals(app.getProjectId())){
                    projectAssigned = true;
                    break;
                }
            }
            if(projectAssigned){
                bookableApps.add(app);
            }
        }


        if (bookableApps.isEmpty()) {
            System.out.println("No successful applications found for the projects you are assigned to.");
            return;
        }

        System.out.println("Select an application to book:");
        displayApplications(bookableApps); // Show list of successful applications
        Application selectedApp = selectApplicationFromList(bookableApps);
        if (selectedApp == null) return;

        FlatType flatTypeToBook;
        try {
            flatTypeToBook = FlatType.valueOf(selectedApp.getFlatType());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid flat type ("+selectedApp.getFlatType()+") associated with application " + selectedApp.getId());
            return;
        }


        if (getConfirmation("Confirm booking of " + flatTypeToBook + " flat for application ID " + selectedApp.getId() + " (Applicant: " + selectedApp.getApplicantId() + ")? (Y/N): ")) {
            officerController.handleBookFlat(officer, selectedApp, flatTypeToBook);
        } else {
            System.out.println("Booking cancelled.");
        }
    }


    // --- Manager Action Handlers ---

    private void handleManagerViewAllProjects(HdbManager manager) {
        System.out.println("\n--- All Projects ---");
        List<Project> projects = managerController.handleViewAllProjects(manager);
        displayProjects(projects);
    }

    private void handleManagerFilterAllProjects(HdbManager manager){
        System.out.println("\n--- Filter All Projects ---");
        Map<String, String> filters = new HashMap<>();
        String input;

        input = getStringInput("Enter Neighbourhood (or leave blank): ");
        if(!input.isEmpty()) filters.put("neighbourhood", input);

        input = getStringInput("Enter Flat Type (TWOROOM/THREEROOM or leave blank): ");
        if(!input.isEmpty()) filters.put("flattype", input);

        input = getStringInput("Enter Project Name contains (or leave blank): ");
        if(!input.isEmpty()) filters.put("projectname", input);

        input = getStringInput("Enter Manager ID (NRIC) (or leave blank): ");
        if(!input.isEmpty()) filters.put("managerid", input);

        List<Project> projects = managerController.managerService.filterAllProjects(filters, manager); // Call service directly
        displayProjects(projects);
    }


    private void handleManagerViewProjectDetails(HdbManager manager) {
        System.out.println("\n--- View Project Details ---");
        List<Project> projects = managerController.handleViewAllProjects(manager);
        if (projects.isEmpty()) return;
        displayProjects(projects);
        Project selectedProject = selectProjectFromList(projects);
        if (selectedProject == null) return;
        // Display more details? The displayProjects method shows most info.
        System.out.println("\n--- Details for Project: " + selectedProject.getProjName() + " ---");
        displayProjectDetails(selectedProject); // Use a more detailed display method
    }

    private void handleCreateProject(HdbManager manager) {
        System.out.println("\n--- Create New Project ---");
        Map<String, Object> details = new HashMap<>();

        details.put("projectName", getStringInput("Enter Project Name: "));
        details.put("neighbourhood", getStringInput("Enter Neighbourhood: "));
        // Use helper for dates
        details.put("startDate", getDateInput("Enter Application Start Date (yyyy-MM-dd): "));
        details.put("endDate", getDateInput("Enter Application End Date (yyyy-MM-dd): "));
        details.put("units2Room", getIntInput("Enter Number of 2-Room Flats (0 if none): ", 0));
        details.put("units3Room", getIntInput("Enter Number of 3-Room Flats (0 if none): ", 0));
        details.put("officerSlots", getIntInput("Enter Number of Officer Slots (0-10): ", 0, 10));
        details.put("isVisible", getConfirmation("Make project visible initially? (Y/N): "));

        // Validate dates are logical (end >= start) - service might do this
        if (details.get("startDate") == null || details.get("endDate") == null) {
            System.out.println("Invalid date entered. Project creation cancelled.");
            return;
        }

        if (getConfirmation("Confirm creation of this project? (Y/N): ")) {
            managerController.handleCreateProject(manager, details);
        } else {
            System.out.println("Project creation cancelled.");
        }
    }

    private void handleEditProject(HdbManager manager) {
        System.out.println("\n--- Edit Project ---");
        // List projects managed by this manager
        List<Project> projects = managerController.managerService.viewProjectsByManager(manager);
        if (projects.isEmpty()) {
            System.out.println("You are not managing any projects.");
            return;
        }
        System.out.println("Select a project you manage to edit:");
        displayProjects(projects);
        Project selectedProject = selectProjectFromList(projects);
        if (selectedProject == null) return;


        Map<String, Object> updates = new HashMap<>();
        System.out.println("Enter new values or leave blank to keep current value.");

        String inputStr; Object inputObj;

        inputStr = getStringInput("New Project Name [" + selectedProject.getProjName() + "]: ");
        if (!inputStr.isEmpty()) updates.put("projectName", inputStr);

        inputStr = getStringInput("New Neighbourhood [" + selectedProject.getNeighbourhood() + "]: ");
        if (!inputStr.isEmpty()) updates.put("neighbourhood", inputStr);

        // Handle dates carefully - allow changing one or both
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentStart = (selectedProject.getAppOpen() != null) ? sdf.format(selectedProject.getAppOpen()) : "N/A";
        String currentEnd = (selectedProject.getAppClose() != null) ? sdf.format(selectedProject.getAppClose()) : "N/A";

        inputObj = getDateInputOptional("New Start Date (yyyy-MM-dd) [" + currentStart + "]: ");
        if (inputObj != null) updates.put("startDate", inputObj);

        inputObj = getDateInputOptional("New End Date (yyyy-MM-dd) [" + currentEnd + "]: ");
        if (inputObj != null) updates.put("endDate", inputObj);


        // Handle flat counts
        int current2R = 0, current3R = 0;
        if (selectedProject.getFlats() != null) {
            for (Flat f : selectedProject.getFlats()) {
                if (f.getFlatType() == FlatType.TWOROOM) current2R = f.getTotal();
                if (f.getFlatType() == FlatType.THREEROOM) current3R = f.getTotal();
            }
        }
        Integer inputInt = getIntInputOptional("New Total 2-Room Units [" + current2R + "]: ", 0);
        if (inputInt != null) updates.put("units2Room", inputInt);

        inputInt = getIntInputOptional("New Total 3-Room Units [" + current3R + "]: ", 0);
        if (inputInt != null) updates.put("units3Room", inputInt);

        inputInt = getIntInputOptional("New Officer Slots [" + selectedProject.getOfficerSlots() + "]: ", 0, 10);
        if (inputInt != null) updates.put("officerSlots", inputInt);

        if (updates.isEmpty()){
            System.out.println("No changes entered. Edit cancelled.");
            return;
        }

        if (getConfirmation("Confirm these edits for project " + selectedProject.getProjectId() + "? (Y/N): ")) {
            managerController.handleEditProject(manager, selectedProject.getProjectId(), updates);
        } else {
            System.out.println("Edit cancelled.");
        }
    }

    private void handleDeleteProject(HdbManager manager) {
        System.out.println("\n--- Delete Project ---");
        List<Project> projects = managerController.managerService.viewProjectsByManager(manager);
        if (projects.isEmpty()) {
            System.out.println("You are not managing any projects to delete.");
            return;
        }
        System.out.println("Select a project you manage to DELETE:");
        displayProjects(projects);
        Project selectedProject = selectProjectFromList(projects);
        if (selectedProject == null) return;

        if (getConfirmation("!!! WARNING !!! Are you absolutely sure you want to DELETE project '" + selectedProject.getProjName() + "' (ID: " + selectedProject.getProjectId() + ")? This cannot be undone. (Y/N): ")) {
            managerController.handleDeleteProject(manager, selectedProject.getProjectId());
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void handleToggleProjectVisibility(HdbManager manager) {
        System.out.println("\n--- Toggle Project Visibility ---");
        List<Project> projects = managerController.managerService.viewProjectsByManager(manager);
        if (projects.isEmpty()) {
            System.out.println("You are not managing any projects.");
            return;
        }
        System.out.println("Select a project to toggle visibility:");
        displayProjects(projects); // Shows current visibility
        Project selectedProject = selectProjectFromList(projects);
        if (selectedProject == null) return;

        boolean currentVisibility = selectedProject.isVisible();
        boolean newVisibility = !currentVisibility;

        if (getConfirmation("Set visibility for project '" + selectedProject.getProjName() + "' to " + newVisibility + "? (Y/N): ")) {
            managerController.handleToggleProjectVisibility(manager, selectedProject.getProjectId(), newVisibility);
        } else {
            System.out.println("Visibility toggle cancelled.");
        }
    }


    private void handleProcessOfficerRegistration(HdbManager manager) {
        System.out.println("\n--- Process Officer Registration Requests ---");
        // Need a way to find PENDING registrations. This requires repository support.
        // Simulate: Find all officers and check their status for pending registrations linked to manager's projects.
        System.out.println("NOTE: This requires searching all officers - listing pending requests is simulated.");
        // List pending requests (Simulated - needs proper query)
        // List<RegistrationRequest> requests = managerService.findPendingOfficerRegistrations(manager);
        // displayPendingOfficerRegistrations(requests);
        // if(requests.isEmpty()) return;
        // Select request...

        // Simplified approach: Manager enters Officer NRIC and Project ID manually
        String officerNric = getStringInput("Enter Officer NRIC to process: ");
        String projectId = getStringInput("Enter Project ID the officer applied for: ");

        // Basic validation
        if (!officerNric.matches("^[STst]\\d{7}[A-Za-z]$") || projectId.isEmpty()) {
            System.out.println("Invalid NRIC or Project ID format.");
            return;
        }

        System.out.println("Process registration for Officer " + officerNric + " on Project " + projectId + ":");
        System.out.println("1. Approve");
        System.out.println("2. Reject");
        int choice = getIntInput("Enter choice (1-2): ", 1, 2);
        if (choice == 0) return; // Invalid input from helper

        boolean approve = (choice == 1);
        String action = approve ? "Approve" : "Reject";

        if (getConfirmation("Confirm " + action + " registration for Officer " + officerNric + " on Project " + projectId + "? (Y/N): ")) {
            managerController.handleProcessOfficerRegistration(manager, officerNric, projectId, approve);
        } else {
            System.out.println("Processing cancelled.");
        }

    }

    private void handleProcessApplicationApproval(HdbManager manager) {
        System.out.println("\n--- Process Application Approval/Rejection ---");
        // Need to find PENDING applications for manager's projects (or all projects?)
        // Simulate: List all PENDING applications system-wide
        List<Application> pendingApps = managerController.managerService.findApplicationsByStatusPlaceholder(ApplStatus.PENDING); // Need repo access

        if (pendingApps == null || pendingApps.isEmpty()) {
            System.out.println("No applications currently pending approval.");
            return;
        }

        System.out.println("Select an application to process:");
        displayApplications(pendingApps);
        Application selectedApp = selectApplicationFromList(pendingApps);
        if (selectedApp == null) return;


        System.out.println("Process application ID " + selectedApp.getId() + " (Applicant: " + selectedApp.getApplicantId() + ", Project: " + selectedApp.getProjectId() + "):");
        System.out.println("1. Approve (Grant Success Status)");
        System.out.println("2. Reject");
        int choice = getIntInput("Enter choice (1-2): ", 1, 2);
        if (choice == 0) return;

        boolean approve = (choice == 1);
        String action = approve ? "Approve" : "Reject";

        if (getConfirmation("Confirm " + action + " application " + selectedApp.getId() + "? (Y/N): ")) {
            managerController.handleProcessApplicationApproval(manager, selectedApp.getId(), approve);
        } else {
            System.out.println("Processing cancelled.");
        }
    }

    private void handleProcessWithdrawal(HdbManager manager) {
        System.out.println("\n--- Process Application Withdrawal Requests ---");
        // Need to find WITHDRAW_PENDING applications
        List<Application> withdrawApps = managerController.managerService.findApplicationsByStatusPlaceholder(ApplStatus.WITHDRAW_PENDING); // Need repo access

        if (withdrawApps == null || withdrawApps.isEmpty()) {
            System.out.println("No applications currently pending withdrawal approval.");
            return;
        }

        System.out.println("Select a withdrawal request to process:");
        displayApplications(withdrawApps);
        Application selectedApp = selectApplicationFromList(withdrawApps);
        if (selectedApp == null) return;

        System.out.println("Process withdrawal for application ID " + selectedApp.getId() + ":");
        System.out.println("1. Approve Withdrawal");
        System.out.println("2. Reject Withdrawal");
        int choice = getIntInput("Enter choice (1-2): ", 1, 2);
        if (choice == 0) return;

        boolean approve = (choice == 1);
        String action = approve ? "Approve" : "Reject";

        if (getConfirmation("Confirm " + action + " withdrawal for application " + selectedApp.getId() + "? (Y/N): ")) {
            managerController.handleProcessWithdrawal(manager, selectedApp.getId(), approve);
        } else {
            System.out.println("Processing cancelled.");
        }
    }

    private void handleManagerViewAllEnquiries(HdbManager manager) {
        System.out.println("\n--- All Enquiries ---");
        List<Enquiry> enquiries = managerController.handleViewAllEnquiries(manager);
        displayEnquiries(enquiries);
    }

    private void handleManagerViewEnquiriesByProject(HdbManager manager) {
        System.out.println("\n--- View Enquiries by Project ---");
        List<Project> projects = managerController.handleViewAllProjects(manager);
        if (projects.isEmpty()) return;
        displayProjects(projects);
        Project selectedProject = selectProjectFromList(projects);
        if (selectedProject == null) return;

        try {
            // Assuming the service needs UUID, convert String ID
            // UUID projectIdUUID = UUID.fromString(selectedProject.getProjectId());
            // List<Enquiry> enquiries = managerController.managerService.viewEnquiriesByProject(projectIdUUID, manager);
            // OR if service handles String ID:
            List<Enquiry> enquiries = managerController.managerService.viewEnquiriesByProject(UUID.fromString(selectedProject.getProjectId()), manager); // Needs service change or UUID conversion

            displayEnquiries(enquiries);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid Project ID format for enquiry lookup.");
        } catch (Exception e) {
            System.err.println("Error fetching enquiries for project: " + e.getMessage());
        }
    }

    private void handleManagerReplyToEnquiry(HdbManager manager) {
        System.out.println("\n--- Reply to Enquiry (Manager) ---");
        // List all unreplied enquiries
        List<Enquiry> allEnquiries = managerController.handleViewAllEnquiries(manager);
        List<Enquiry> unrepliedEnquiries = new ArrayList<>();
        if(allEnquiries != null){
            for(Enquiry e : allEnquiries){
                if(e.getReply() == null || e.getReply().isEmpty()){
                    unrepliedEnquiries.add(e);
                }
            }
        }


        if (unrepliedEnquiries.isEmpty()) {
            System.out.println("No enquiries requiring a reply found.");
            return;
        }

        System.out.println("Select an enquiry to reply to:");
        displayEnquiries(unrepliedEnquiries); // Show project ID with enquiry
        Enquiry selectedEnquiry = selectEnquiryFromList(unrepliedEnquiries);
        if (selectedEnquiry == null) return;

        String replyText = getStringInput("Enter your reply: ");
        if (!replyText.trim().isEmpty()) {
            if (getConfirmation("Submit this reply for enquiry ID " + selectedEnquiry.getEnquiryId() + "? (Y/N): ")) {
                managerController.handleManagerReplyToEnquiry(manager, selectedEnquiry.getEnquiryId(), replyText);
            } else {
                System.out.println("Reply cancelled.");
            }
        } else {
            System.out.println("Reply text cannot be empty.");
        }
    }


    private void handleGenerateBookingReport(HdbManager manager) {
        System.out.println("\n--- Generate Booking Report ---");
        Map<String, String> filters = new HashMap<>();
        String input;

        System.out.println("Enter filter criteria (leave blank to ignore):");

        input = getStringInput(" Filter by Project Name: ");
        if(!input.isEmpty()) filters.put("projectName", input);

        input = getStringInput(" Filter by Neighbourhood: ");
        if(!input.isEmpty()) filters.put("neighbourhood", input);

        input = getStringInput(" Filter by Flat Type (TWOROOM/THREEROOM): ");
        if(!input.isEmpty()) filters.put("flatType", input);

        input = getStringInput(" Filter by Marital Status (SINGLE/MARRIED): ");
        if(!input.isEmpty()) filters.put("maritalStatus", input);

        // Add date range filters if needed (e.g., based on booking date stored in Receipt)
        // input = getDateInputOptional(" Filter Start Date (yyyy-MM-dd): ");
        // if(input != null) filters.put("startDate", input);
        // input = getDateInputOptional(" Filter End Date (yyyy-MM-dd): ");
        // if(input != null) filters.put("endDate", input);

        System.out.println("Generating report...");
        List<Object> reportData = managerController.handleGenerateBookingReport(manager, filters);
        displayReportData(reportData); // Specific display for report
    }

    // --- Selection Helpers ---

    private Project selectProjectFromList(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            System.out.println("No projects to select from.");
            return null;
        }
        while (true) {
            String projectId = getStringInput("Enter Project ID to select (or type 'cancel'): ");
            if (projectId.equalsIgnoreCase("cancel")) return null;

            for (Project p : projects) {
                if (p.getProjectId().equalsIgnoreCase(projectId)) {
                    return p;
                }
            }
            System.out.println("Invalid Project ID. Please choose from the list above.");
        }
    }

    private Enquiry selectEnquiryFromList(List<Enquiry> enquiries) {
        if (enquiries == null || enquiries.isEmpty()) {
            System.out.println("No enquiries to select from.");
            return null;
        }
        while (true) {
            String enquiryId = getStringInput("Enter Enquiry ID to select (or type 'cancel'): ");
            if (enquiryId.equalsIgnoreCase("cancel")) return null;

            for (Enquiry e : enquiries) {
                if (e.getEnquiryId().equalsIgnoreCase(enquiryId)) {
                    return e;
                }
            }
            System.out.println("Invalid Enquiry ID. Please choose from the list above.");
        }
    }

    private Application selectApplicationFromList(List<Application> applications) {
        if (applications == null || applications.isEmpty()) {
            System.out.println("No applications to select from.");
            return null;
        }
        while (true) {
            String appId = getStringInput("Enter Application ID to select (or type 'cancel'): ");
            if (appId.equalsIgnoreCase("cancel")) return null;

            for (Application a : applications) {
                if (a.getId().equalsIgnoreCase(appId)) {
                    return a;
                }
            }
            System.out.println("Invalid Application ID. Please choose from the list above.");
        }
    }


    // --- Display Helpers ---

    public void displayMessage(String message) {
        System.out.println(">> " + message);
    }

    private void displayProjects(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            // Caller function should print a message if the list is empty
            // System.out.println("No projects to display.");
            return;
        }
        String border = "+--------------------------------------+-----------------+--------------------------------------+------------+----------+-------+";
        System.out.println(border);
        System.out.printf("| %-36s | %-15s | %-36s | %-10s | %-8s | %-5s |%n", "Project ID", "Name", "Neighbourhood", "App Dates", "Visible?", "Slots");
        System.out.println(border);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Project p : projects) {
            String dateRange = "N/A";
            if(p.getAppOpen() != null && p.getAppClose() != null){
                dateRange = sdf.format(p.getAppOpen()) + " to " + sdf.format(p.getAppClose());
            }
            System.out.printf("| %-36s | %-15s | %-36s | %-25s | %-8s | %-5d |%n", // Adjusted date width
                    p.getProjectId(),
                    truncateString(p.getProjName(), 15),
                    truncateString(p.getNeighbourhood(), 36),
                    dateRange,
                    p.isVisible() ? "Yes" : "No",
                    p.getOfficerSlots());

            // Display flat types and counts below project line
            if (p.getFlats() != null && !p.getFlats().isEmpty()) {
                System.out.print("|    Flats: ");
                boolean firstFlat = true;
                for (Flat flat : p.getFlats()) {
                    if (!firstFlat) System.out.print(", ");
                    System.out.print(flat.getFlatType() + " (" + flat.getRemaining() + "/" + flat.getTotal() + ")");
                    firstFlat = false;
                }
                System.out.println(); // Newline after flats
            } else {
                System.out.println("|    Flats: None defined");
            }
            System.out.println(border); // Separator line after each project
        }
        // System.out.println(border); // Footer border
    }

    private void displayProjectDetails(Project p) {
        if (p == null) {
            System.out.println("No project details to display.");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("  Project ID: " + p.getProjectId());
        System.out.println("  Name: " + p.getProjName());
        System.out.println("  Neighbourhood: " + p.getNeighbourhood());
        String startDate = (p.getAppOpen() != null) ? sdf.format(p.getAppOpen()) : "N/A";
        String endDate = (p.getAppClose() != null) ? sdf.format(p.getAppClose()) : "N/A";
        System.out.println("  Application Period: " + startDate + " to " + endDate);
        System.out.println("  Visibility: " + (p.isVisible() ? "Visible" : "Hidden"));
        System.out.println("  Officer Slots: " + p.getOfficerSlots());
        System.out.println("  Manager: " + ((p.getManager() != null) ? p.getManager().getName() + " ("+p.getManager().getId()+")" : "N/A"));
        System.out.println("  Flats Available:");
        if (p.getFlats() != null && !p.getFlats().isEmpty()) {
            for (Flat flat : p.getFlats()) {
                System.out.println("    - Type: " + flat.getFlatType());
                System.out.println("      Total Units: " + flat.getTotal());
                System.out.println("      Remaining Units: " + flat.getRemaining());
                // System.out.println("      Price: $" + flat.getPrice()); // Add if price is available
            }
        } else {
            System.out.println("    None defined.");
        }
        // Add assigned officers if needed
    }

    private void displayEnquiries(List<Enquiry> enquiries) {
        if (enquiries == null || enquiries.isEmpty()) {
            // System.out.println("No enquiries to display."); // Caller should handle this
            return;
        }
        String border = "+--------------------------------------+-----------------+--------------------------------------+------------------------------------------+------------------------------------------+";
        System.out.println(border);
        System.out.printf("| %-36s | %-15s | %-36s | %-40s | %-40s |%n", "Enquiry ID", "Applicant ID", "Project ID", "Message", "Reply");
        System.out.println(border);
        for (Enquiry e : enquiries) {
            System.out.printf("| %-36s | %-15s | %-36s | %-40s | %-40s |%n",
                    e.getEnquiryId(),
                    e.getApplicantId(),
                    e.getProjectId(),
                    truncateString(e.getMessage(), 40),
                    truncateString(e.getReply(), 40)); // Truncate long messages/replies
        }
        System.out.println(border);
    }

    private void displayApplication(Application app) {
        if (app == null) {
            // System.out.println("No application details to display."); // Caller handles message
            return;
        }
        System.out.println("  Application ID: " + app.getId());
        System.out.println("  Applicant ID:   " + app.getApplicantId());
        System.out.println("  Project ID:     " + app.getProjectId());
        System.out.println("  Flat Type:      " + app.getFlatType());
        System.out.println("  Status:         " + app.getStatus());
        // Add timestamp if available
    }

    private void displayApplications(List<Application> applications) {
        if (applications == null || applications.isEmpty()) {
            // System.out.println("No applications to display.");
            return;
        }
        String border = "+--------------------------------------+-----------------+--------------------------------------+------------+---------------+";
        System.out.println(border);
        System.out.printf("| %-36s | %-15s | %-36s | %-10s | %-13s |%n", "Application ID", "Applicant ID", "Project ID", "Flat Type", "Status");
        System.out.println(border);
        for (Application app : applications) {
            System.out.printf("| %-36s | %-15s | %-36s | %-10s | %-13s |%n",
                    app.getId(),
                    app.getApplicantId(),
                    app.getProjectId(),
                    app.getFlatType(),
                    app.getStatus());
        }
        System.out.println(border);
    }

    // Simple report display - needs enhancement based on actual report data structure
    private void displayReportData(List<Object> reportData) {
        if (reportData == null || reportData.isEmpty()) {
            System.out.println("No report data generated.");
            return;
        }
        System.out.println("\n--- Booking Report ---");
        // Assuming reportData contains Application objects for now
        // A dedicated ReportItem class would be much better
        String border = "+--------------------------------------+-----------------+--------------------------------------+------------+----------------+----------+"; // Added Marital, Age
        System.out.println(border);
        System.out.printf("| %-36s | %-15s | %-36s | %-10s | %-14s | %-8s |%n", "Application ID", "Applicant ID", "Project ID", "Flat Type", "Marital Status", "Age");
        System.out.println(border);

        for (Object item : reportData) {
            if (item instanceof Application) {
                Application app = (Application) item;
                // Need to fetch Applicant details for Marital Status and Age - Inefficient here!
                // This data should ideally be pre-fetched by the service or joined in the repo query
                Applicant applicant = (Applicant) managerController.managerService.findUserByNricPlaceholder(app.getApplicantId()); // Simulation
                String maritalStatus = (applicant != null && applicant.getMaritalStatus() != null) ? applicant.getMaritalStatus().name() : "N/A";
                String age = (applicant != null && applicant.getDob() != null) ? String.valueOf(calculateAge(applicant.getDob())) : "N/A"; // Need calculateAge method

                System.out.printf("| %-36s | %-15s | %-36s | %-10s | %-14s | %-8s |%n",
                        app.getId(),
                        app.getApplicantId(),
                        app.getProjectId(),
                        app.getFlatType(),
                        maritalStatus,
                        age);
            } else {
                System.out.println("| Invalid report item type: " + item.getClass().getSimpleName());
            }
        }
        System.out.println(border);
        System.out.println(" Total Records Found: " + reportData.size());
        System.out.println(" (Note: Report details like Age/Marital Status require additional data lookup in this view simulation)");
    }


    // --- Utility Methods ---

    /**
     * Gets integer input from the user, ensuring it's within optional bounds.
     * Reprompts on invalid input.
     * @param prompt Message to display to the user.
     * @param min    Minimum allowed value (inclusive).
     * @param max    Maximum allowed value (inclusive).
     * @return The valid integer input, or 0 if input is invalid after retries (or initial prompt fails).
     */
    private int getIntInput(String prompt, int min, int max) {
        int input = 0;
        while (true) {
            System.out.print(prompt);
            try {
                input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Input must be between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Gets integer input from the user. Reprompts on invalid input.
     * @param prompt Message to display to the user.
     * @return The valid integer input, or 0 if input is invalid after retries.
     */
    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private int getIntInput(String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value >= min) {
                    return value;
                } else {
                    System.out.println("Value must be " + min + " or greater.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }


    /**
     * Gets integer input, allowing empty input.
     * @param prompt Prompt message including current value.
     * @param min    Minimum value if input is given.
     * @return Integer value or null if input is empty. Throws NumberFormatException if invalid non-empty input.
     */
    private Integer getIntInputOptional(String prompt, int min) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        if (line.trim().isEmpty()) {
            return null; // No change
        }
        try {
            int value = Integer.parseInt(line);
            if (value >= min) {
                return value;
            } else {
                System.out.println("Value must be " + min + " or greater.");
                throw new NumberFormatException("Value below minimum"); // Signal error to caller
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format entered.");
            throw e; // Re-throw to indicate failure
        }
    }
    private Integer getIntInputOptional(String prompt, int min, int max) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        if (line.trim().isEmpty()) {
            return null; // No change
        }
        try {
            int value = Integer.parseInt(line);
            if (value >= min && value <= max) {
                return value;
            } else {
                System.out.println("Value must be between " + min + " and " + max +".");
                throw new NumberFormatException("Value out of range"); // Signal error to caller
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format entered.");
            throw e; // Re-throw to indicate failure
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    // Basic password input - does not mask characters in standard console
    private String getPasswordInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
        /* // Proper masking requires Console class, which might not be available (e.g., in IDEs)
         Console console = System.console();
         if (console == null) {
             System.out.println("Warning: Console not available, password will be visible.");
             System.out.print(prompt);
             return scanner.nextLine();
         } else {
             char[] passwordArray = console.readPassword(prompt);
             return new String(passwordArray);
         }
        */
    }

    private boolean getConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'Y' or 'N'.");
            }
        }
    }

    private String getDateInput(String prompt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false); // Disallow invalid dates like 2023-02-30
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                // Try parsing to validate format and date validity
                Date date = sdf.parse(input);
                // Return the valid string representation
                return sdf.format(date); // Return formatted string to ensure consistency
            } catch (ParseException e) {
                System.out.println("Invalid date format or date value. Please use yyyy-MM-dd.");
            }
        }
    }

    private String getDateInputOptional(String prompt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) {
                return null; // Allow empty input
            }
            try {
                Date date = sdf.parse(input);
                return sdf.format(date);
            } catch (ParseException e) {
                System.out.println("Invalid date format or date value. Please use yyyy-MM-dd or leave blank.");
                // Loop again
            }
        }
    }

    /**
     * Truncates a string to a maximum length, adding ellipsis if truncated.
     * @param text The string to truncate.
     * @param maxLength The maximum desired length (must be > 3 for ellipsis).
     * @return The truncated string (or original if shorter/equal to maxLength).
     */
    private String truncateString(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength <= 3) return (text.length() > maxLength) ? text.substring(0, maxLength) : text; // Cannot add ellipsis
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    // Helper to calculate age (duplicate from service for view layer convenience, or make accessible)
    private int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        Calendar today = Calendar.getInstance();
        Calendar birthDay = Calendar.getInstance();
        birthDay.setTime(birthDate);

        int age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        // Adjust age if birthday hasn't occurred yet this year
        if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

}