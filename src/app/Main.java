package app;

import controller.*;
import entity.*;
import repository.*;
import service.*;
import view.CLIView;

/**
 * Main application class to bootstrap the BTO Management System.
 * Initializes repositories, services, controllers, and the view, then starts the application.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting BTO Management System...");

        // Add debugging
        System.out.println("Debug: Initializing repositories...");
        
        try {
            // Initialize repositories
            ApplicantRepo applicantRepo = new ApplicantRepo();
            System.out.println("Debug: ApplicantRepo initialized");
            
            HdbManagerRepo managerRepo = new HdbManagerRepo();
            System.out.println("Debug: HdbManagerRepo initialized");
            
            HdbOfficerRepo officerRepo = new HdbOfficerRepo();
            System.out.println("Debug: HdbOfficerRepo initialized");
            
            ProjectRepo projectRepo = new ProjectRepo(managerRepo, officerRepo);
            System.out.println("Debug: ProjectRepo initialized");
            
            ApplicationRepo applicationRepo = new ApplicationRepo();
            System.out.println("Debug: ApplicationRepo initialized");
            
            EnquiryRepo enquiryRepo = new EnquiryRepo();
            System.out.println("Debug: EnquiryRepo initialized");

        // Initialize services
        UserService userService = new UserService(applicantRepo, managerRepo, officerRepo);
        
        ApplicantService applicantService = new ApplicantService(
                applicantRepo, projectRepo, applicationRepo, enquiryRepo);
        
        HdbOfficerService officerService = new HdbOfficerService(
                officerRepo, projectRepo, applicationRepo, enquiryRepo, applicantRepo);
        
        HdbManagerService managerService = new HdbManagerService(
                managerRepo, projectRepo, applicationRepo, enquiryRepo, officerRepo);

        // Initialize controllers
        UserController userController = new UserController(userService);
        
        ApplicantController applicantController = new ApplicantController(
                applicantService, userService);
        
        HdbOfficerController officerController = new HdbOfficerController(
                officerService, userService);
        
        HdbManagerController managerController = new HdbManagerController(
                managerService, userService);

        // Initialize and start CLI view
        CLIView cliView = new CLIView(
                userController, applicantController, officerController, managerController);
        
        System.out.println("Debug: CLIView initialized");
                
        // Start application
        cliView.run();
        
        System.out.println("Exiting BTO Management System. Goodbye!");
        } catch (Exception e) {
            System.err.println("Debug: Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}