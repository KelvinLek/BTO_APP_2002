package app;

import service.*;
import controller.*;
import view.CLIView;

/**
 * Main application class to bootstrap the BTO Management System.
 * Initializes services, controllers, and the view, then starts the application loop.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Starting BTO Management System...");

        // --- Service Instantiation ---
        // In a real application, these services would likely be injected
        // with repository instances. Here, they use their internal placeholders.
        // IMPORTANT: Ensure these service classes have constructors that work
        // without repository arguments for this placeholder setup.
        // If they require repo interfaces, you might need to pass null or mock objects.
        // Based on previous generation, they extend UserService which needs IAuthService/IPasswordService
        // and implement their own interfaces. Let's assume they have workable default constructors or
        // constructors that can accept themselves as implementations of the needed interfaces.

        // Create concrete service instances
        ApplicantService applicantService = new ApplicantService(); // Assumes default constructor works for placeholder version
        HdbOfficerService hdbOfficerService = new HdbOfficerService(); // Assumes default constructor
        HdbManagerService hdbManagerService = new HdbManagerService(); // Assumes default constructor

        // --- Controller Instantiation ---
        // Inject the corresponding services into the controllers.
        // Note: UserController is the base, but login might be handled before specific roles.
        // We instantiate all role controllers and pass them to the view.

        // Create a generic UserController instance primarily for login/password change before role is known
        // Pass the respective service instances that implement the required interfaces
        UserController userController = new UserController(applicantService, applicantService); // ApplicantService implements IAuthService, IPasswordService

        ApplicantController applicantController = new ApplicantController(
                applicantService, applicantService, applicantService); // ApplicantService provides all needed interfaces for itself and base

        HdbOfficerController hdbOfficerController = new HdbOfficerController(
                hdbOfficerService, applicantService, hdbOfficerService, hdbOfficerService); // OfficerService provides officer methods and inherits/implements others

        HdbManagerController hdbManagerController = new HdbManagerController(
                hdbManagerService, hdbManagerService, hdbManagerService); // ManagerService provides all needed interfaces for itself and base

        // --- View Instantiation ---
        // Inject all controllers into the view.
        CLIView cliView = new CLIView(userController, applicantController, hdbOfficerController, hdbManagerController);

        // --- Start Application ---
        cliView.run();

        System.out.println("Exiting BTO Management System. Goodbye!");
    }
}