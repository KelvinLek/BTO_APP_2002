package service;

import entity.*;
import pub_enums.*; // ApplStatus, FlatType, MaritalStatus
import java.util.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
// Import necessary repository interfaces if interacting with data storage
// import repository.*;

/**
 * Provides services specific to BTO Applicants, handling project viewing,
 * applications, status checks, withdrawals, enquiries, and eligibility.
 */
public class ApplicantService extends UserService implements IApplicantService, IEligibilityCheck, IEnquiryService, IApplicantEnquiryView, IApplicantProjectView {

    // Assume repository injection if data persistence is implemented
    // private final IApplicantRepo applicantRepo; // Assumes Applicant specific repo
    // private final IApplicationRepo applicationRepo;
    // private final IProjectRepo projectRepo;
    // private final IEnquiryRepo enquiryRepo;
    //
    // public ApplicantService(IUserRepo userRepo, IApplicantRepo applicantRepo, IApplicationRepo applicationRepo, IProjectRepo projectRepo, IEnquiryRepo enquiryRepo) {
    //     super(userRepo); // Pass userRepo to parent
    //     this.applicantRepo = applicantRepo;
    //     this.applicationRepo = applicationRepo;
    //     this.projectRepo = projectRepo;
    //     this.enquiryRepo = enquiryRepo;
    // }

    // --- IApplicantService Implementation ---

    /**
     * Retrieves projects visible and eligible for the applicant.
     * Considers project visibility, application period, and applicant eligibility.
     *
     * @param applicant The Applicant making the request.
     * @return A List of eligible Project objects.
     */
    @Override
    public List<Project> viewAvailableProjects(Applicant applicant) {
        if (applicant == null) return Collections.emptyList();

        // 1. Get all potentially visible projects (replace with actual repo call)
        // List<Project> allProjects = projectRepo.findAll(); // Example repo call
        List<Project> allProjects = findAllProjectsPlaceholder(); // Placeholder

        List<Project> availableProjects = new ArrayList<>();
        Date today = new Date(); // Use current date for checking application period

        for (Project project : allProjects) {
            // 2. Check project visibility and application period
            if (project.isVisible() && project.getAppOpen() != null && project.getAppClose() != null &&
                    !today.before(project.getAppOpen()) && !today.after(project.getAppClose())) {

                // 3. Check if applicant is eligible for *any* flat type in this project
                boolean eligibleForAnyFlat = false;
                if (project.getFlats() != null) {
                    for (Flat flat : project.getFlats()) {
                        if (checkEligibility(applicant, flat.getFlatType())) {
                            eligibleForAnyFlat = true;
                            break;
                        }
                    }
                }
                if (eligibleForAnyFlat) {
                    availableProjects.add(project);
                }
            }
        }
        return availableProjects;
    }

    /**
     * Submits a BTO application after performing eligibility checks.
     *
     * @param applicant The Applicant applying.
     * @param project   The Project being applied for.
     * @param flatType  The specific FlatType.
     * @return true if the application was submitted successfully.
     * @throws IllegalArgumentException For eligibility violations or invalid data.
     * @throws NoSuchElementException   If applicant/project not found.
     * @throws Exception                For persistence errors.
     */
    @Override
    public boolean applyForProject(Applicant applicant, Project project, FlatType flatType) throws IllegalArgumentException, NoSuchElementException, Exception {
        if (applicant == null || project == null || flatType == null) {
            throw new IllegalArgumentException("Applicant, Project, and FlatType cannot be null.");
        }
        if (applicant.getId() == null || project.getProjectId() == null){
            throw new NoSuchElementException("Applicant or Project ID missing.");
        }

        // 1. Check if project is open for application
        Date today = new Date();
        if (project.getAppOpen() == null || project.getAppClose() == null ||
                today.before(project.getAppOpen()) || today.after(project.getAppClose()) || !project.isVisible()) {
            throw new IllegalArgumentException("Project '" + project.getProjName() + "' is not currently open for applications.");
        }

        // 2. Check if applicant already has an active (non-final) application
        // Application existingApp = applicationRepo.findActiveByApplicantId(applicant.getId()); // Example repo call
        Application existingApp = findActiveApplicationByApplicantPlaceholder(applicant.getId()); // Placeholder
        if (existingApp != null) {
            throw new IllegalArgumentException("Applicant already has an active application (ID: " + existingApp.getId() + "). Cannot apply for another project.");
        }

        // 3. Check specific eligibility for the chosen flat type
        if (!checkEligibility(applicant, flatType)) {
            int age = calculateAge(applicant.getDob());
            throw new IllegalArgumentException("Applicant (Age: " + age + ", Status: " + applicant.getMaritalStatus() + ") is not eligible for flat type: " + flatType);
        }

        // 4. Check if the project actually offers this flat type
        boolean offersFlatType = false;
        if (project.getFlats() != null) {
            for (Flat flat : project.getFlats()) {
                if (flat.getFlatType() == flatType) {
                    offersFlatType = true;
                    break;
                }
            }
        }
        if (!offersFlatType) {
            throw new IllegalArgumentException("Project '" + project.getProjName() + "' does not offer the selected flat type: " + flatType);
        }


        // 5. Create and save the new application
        // Use UUID for unique Application ID
        Application newApplication = new Application(
                UUID.randomUUID().toString(),
                ApplStatus.PENDING,
                applicant.getId(),
                project.getProjectId(), // Assuming project ID is String
                flatType.name() // Store enum name as string
        );

        try {
            // applicationRepo.save(newApplication); // Example repo call
            saveApplicationPlaceholder(newApplication); // Placeholder
            System.out.println("Application submitted successfully (ID: " + newApplication.getId() + ")");
            // Associate application with applicant (if entity design requires it)
            applicant.setApplication(newApplication);
            // saveUserPlaceholder(applicant); // If saving user updates association
            return true;
        } catch (Exception e) {
            System.err.println("Error saving application: " + e.getMessage());
            throw new Exception("Failed to save the application.", e);
        }
    }

    /**
     * Retrieves the current application status for the applicant.
     *
     * @param applicant The Applicant.
     * @return The current Application object, or null if none found.
     */
    @Override
    public Application getApplicationStatus(Applicant applicant) {
        if (applicant == null || applicant.getId() == null) return null;
        // Application app = applicationRepo.findActiveByApplicantId(applicant.getId()); // Example repo call
        // If not found active, maybe find latest? Repo logic needed.
        Application app = findActiveApplicationByApplicantPlaceholder(applicant.getId()); // Placeholder
        return app;
        // Alternatively, return applicant.getApplication() if the entity holds the current one.
    }

    /**
     * Requests withdrawal for the specified application. Sets status to WITHDRAW_PENDING.
     *
     * @param application The Application to withdraw.
     * @return true if the request was logged successfully.
     * @throws NoSuchElementException If application is invalid or not found.
     * @throws Exception              For persistence errors.
     */
    @Override
    public boolean requestWithdrawal(Application application) throws NoSuchElementException, Exception {
        if (application == null || application.getId() == null) {
            throw new NoSuchElementException("Invalid application provided for withdrawal.");
        }

        // Find the application to ensure it exists and check its current state
        // Application appToWithdraw = applicationRepo.findById(application.getId()); // Example repo call
        Application appToWithdraw = findApplicationByIdPlaceholder(application.getId()); // Placeholder
        if(appToWithdraw == null) {
            throw new NoSuchElementException("Application with ID " + application.getId() + " not found.");
        }

        // Check if withdrawal is allowed (e.g., not already withdrawn/rejected?)
        ApplStatus currentStatus = appToWithdraw.getStatus();
        if (currentStatus == ApplStatus.WITHDRAW_APPROVED || currentStatus == ApplStatus.REJECT /* Add other final states if needed */) {
            System.out.println("Application " + appToWithdraw.getId() + " is already in a final state ("+ currentStatus +") and cannot be withdrawn.");
            return false;
        }
        if (currentStatus == ApplStatus.WITHDRAW_PENDING) {
            System.out.println("Application " + appToWithdraw.getId() + " is already pending withdrawal.");
            return true; // Already requested
        }


        // Update status to pending withdrawal
        appToWithdraw.setStatus(ApplStatus.WITHDRAW_PENDING);

        try {
            // applicationRepo.save(appToWithdraw); // Example repo call
            saveApplicationPlaceholder(appToWithdraw); // Placeholder
            System.out.println("Withdrawal requested for application ID: " + appToWithdraw.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error saving withdrawal request: " + e.getMessage());
            // Revert status if save fails? Complex transaction logic might be needed.
            // appToWithdraw.setStatus(currentStatus); // Example rollback (simplified)
            throw new Exception("Failed to save withdrawal request.", e);
        }
    }


    // --- IEligibilityCheck Implementation ---

    /**
     * Calculates the age of the applicant based on Date of Birth.
     * @param birthDate The applicant's Date of Birth.
     * @return The age in years, or 0 if birthDate is null.
     */
    private int calculateAge(Date birthDate) {
        if (birthDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDate birthday = birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return Period.between(birthday, today).getYears();
    }

    /**
     * Checks if an applicant is eligible for a specific flat type based on age and marital status.
     * Rules: Single >= 35 (2-Room only), Married >= 21 (Any).
     *
     * @param applicant The Applicant.
     * @param flatType  The FlatType being considered.
     * @return true if eligible, false otherwise.
     * @throws NoSuchElementException If applicant data is missing/invalid.
     */
    @Override
    public boolean checkEligibility(Applicant applicant, FlatType flatType) throws NoSuchElementException {
        if (applicant == null || applicant.getDob() == null || applicant.getMaritalStatus() == null) {
            throw new NoSuchElementException("Applicant data (DOB, Marital Status) is incomplete for eligibility check.");
        }

        int age = calculateAge(applicant.getDob());
        MaritalStatus maritalStatus = applicant.getMaritalStatus();

        if (maritalStatus == MaritalStatus.SINGLE) {
            return age >= 35 && flatType == FlatType.TWOROOM;
        } else if (maritalStatus == MaritalStatus.MARRIED) {
            // Assuming married can apply for any type offered (2-room or 3-room based on brief)
            return age >= 21 && (flatType == FlatType.TWOROOM || flatType == FlatType.THREEROOM);
        }

        return false; // Not single or married, or doesn't meet criteria
    }

    /**
     * Checks if an applicant is eligible to apply for a *project* (eligible for any offered flat type)
     * and doesn't have an active application.
     *
     * @param applicant The Applicant.
     * @param project   The Project.
     * @return true if eligible to apply, false otherwise.
     * @throws NoSuchElementException If applicant/project data is invalid.
     */
    @Override
    public boolean checkProjectApplicationEligibility(Applicant applicant, Project project) throws NoSuchElementException {
        if (applicant == null || project == null) {
            throw new NoSuchElementException("Applicant or Project cannot be null.");
        }
        if (project.getFlats() == null || project.getFlats().isEmpty()) {
            // Optional: Treat projects with no defined flats as ineligible?
            System.out.println("Warning: Project " + project.getProjName() + " has no flats defined.");
            return false;
        }

        // 1. Check if eligible for at least one flat type offered by the project
        boolean eligibleForAnyFlat = false;
        for (Flat flat : project.getFlats()) {
            if (checkEligibility(applicant, flat.getFlatType())) {
                eligibleForAnyFlat = true;
                break;
            }
        }
        if (!eligibleForAnyFlat) {
            return false; // Not eligible for any flat in this project
        }

        // 2. Check if applicant already has an active application
        // Application existingApp = applicationRepo.findActiveByApplicantId(applicant.getId()); // Example repo call
        Application existingApp = findActiveApplicationByApplicantPlaceholder(applicant.getId()); // Placeholder
        if (existingApp != null) {
            return false; // Already has an active application
        }

        return true; // Eligible for a flat and no active application
    }

    // --- IEnquiryService Implementation ---

    /**
     * Submits a new enquiry for a project.
     *
     * @param user    The User (Applicant) submitting.
     * @param project The Project enquired about.
     * @param message The enquiry message.
     * @return The created Enquiry object.
     * @throws NoSuchElementException If user/project is invalid.
     * @throws Exception              If persistence fails.
     */
    @Override
    public Enquiry submitEnquiry(User user, Project project, String message) throws NoSuchElementException, Exception {
        if (user == null || project == null || message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("User, project, and message are required to submit an enquiry.");
        }
        if (user.getId() == null || project.getProjectId() == null){
            throw new NoSuchElementException("User or Project ID is missing.");
        }
        // Ensure the user is an Applicant (or maybe Officer too? Brief implies Applicant creates)
        if (user.getRole() != Role.APPLICANT && user.getRole() != Role.HDBOFFICER /* Allow officer? */) {
            throw new SecurityException("Only Applicants can submit enquiries through this service.");
        }


        // Create new Enquiry object
        Enquiry newEnquiry = new Enquiry(
                UUID.randomUUID().toString(), // Generate unique ID
                user.getId(),
                project.getProjectId(), // Assuming project ID is String
                message,
                null // No reply initially
        );

        try {
            // enquiryRepo.save(newEnquiry); // Example repo call
            saveEnquiryPlaceholder(newEnquiry); // Placeholder
            // Optional: Add enquiry to user's list if entity relationship exists
            if (user instanceof Applicant) {
                ((Applicant) user).addEnquiry(newEnquiry);
                // saveUserPlaceholder(user); // If saving user updates association
            }
            System.out.println("Enquiry submitted successfully (ID: " + newEnquiry.getEnquiryId() + ")");
            return newEnquiry;
        } catch (Exception e) {
            System.err.println("Error saving enquiry: " + e.getMessage());
            throw new Exception("Failed to submit enquiry.", e);
        }
    }

    /**
     * Edits an existing enquiry's message. Only the owner can edit.
     *
     * @param enquiry    The Enquiry object (containing ID).
     * @param newMessage The new message.
     * @param editor     The User attempting the edit.
     * @return true if edit successful.
     * @throws NoSuchElementException If enquiry not found.
     * @throws SecurityException      If editor is not the owner.
     * @throws Exception              If persistence fails.
     */
    @Override
    public boolean editEnquiry(Enquiry enquiry, String newMessage, User editor) throws NoSuchElementException, SecurityException, Exception {
        if (enquiry == null || enquiry.getEnquiryId() == null || newMessage == null || newMessage.trim().isEmpty() || editor == null || editor.getId() == null) {
            throw new IllegalArgumentException("Enquiry ID, new message, and editor ID are required.");
        }

        // 1. Find the existing enquiry
        // Enquiry existingEnquiry = enquiryRepo.findById(enquiry.getEnquiryId()); // Example repo call
        Enquiry existingEnquiry = findEnquiryByIdPlaceholder(enquiry.getEnquiryId()); // Placeholder
        if (existingEnquiry == null) {
            throw new NoSuchElementException("Enquiry with ID " + enquiry.getEnquiryId() + " not found.");
        }

        // 2. Check ownership
        if (!existingEnquiry.getApplicantId().equals(editor.getId())) {
            throw new SecurityException("User " + editor.getId() + " does not have permission to edit enquiry " + existingEnquiry.getEnquiryId());
        }

        // 3. Update the message
        existingEnquiry.setMessage(newMessage);

        // 4. Save the updated enquiry
        try {
            // enquiryRepo.save(existingEnquiry); // Example repo call
            saveEnquiryPlaceholder(existingEnquiry); // Placeholder
            System.out.println("Enquiry " + existingEnquiry.getEnquiryId() + " edited successfully.");
            return true;
        } catch (Exception e) {
            System.err.println("Error saving edited enquiry: " + e.getMessage());
            // Consider rolling back the message change in the object if save fails
            // existingEnquiry.setMessage(originalMessage); // Example rollback
            throw new Exception("Failed to save edited enquiry.", e);
        }
    }

    /**
     * Deletes an existing enquiry. Only the owner can delete.
     *
     * @param enquiryId The ID of the enquiry to delete.
     * @param deleter   The User attempting deletion.
     * @return true if deletion successful.
     * @throws NoSuchElementException If enquiry not found.
     * @throws SecurityException      If deleter is not the owner.
     * @throws Exception              If persistence fails.
     */
    @Override
    public boolean deleteEnquiry(String enquiryId, User deleter) throws NoSuchElementException, SecurityException, Exception {
        if (enquiryId == null || deleter == null || deleter.getId() == null) {
            throw new IllegalArgumentException("Enquiry ID and deleter ID are required.");
        }

        // 1. Find the existing enquiry
        // Enquiry existingEnquiry = enquiryRepo.findById(enquiryId); // Example repo call
        Enquiry existingEnquiry = findEnquiryByIdPlaceholder(enquiryId); // Placeholder
        if (existingEnquiry == null) {
            throw new NoSuchElementException("Enquiry with ID " + enquiryId + " not found.");
        }

        // 2. Check ownership
        if (!existingEnquiry.getApplicantId().equals(deleter.getId())) {
            throw new SecurityException("User " + deleter.getId() + " does not have permission to delete enquiry " + enquiryId);
        }

        // 3. Delete the enquiry
        try {
            // enquiryRepo.deleteById(enquiryId); // Example repo call
            deleteEnquiryPlaceholder(enquiryId); // Placeholder
            // Optional: Remove from user's list if entity relationship exists
            if (deleter instanceof Applicant) {
                // Need a removeEnquiry method in Applicant entity or handle here
                ((Applicant) deleter).removeEnquiry(existingEnquiry);
                // saveUserPlaceholder(deleter); // If saving user updates association
            }
            System.out.println("Enquiry " + enquiryId + " deleted successfully.");
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting enquiry: " + e.getMessage());
            throw new Exception("Failed to delete enquiry.", e);
        }
    }

    // --- IApplicantEnquiryView Implementation ---

    /**
     * Retrieves all enquiries submitted by a specific applicant.
     *
     * @param applicant The Applicant.
     * @return A List of their Enquiry objects.
     */
    @Override
    public List<Enquiry> viewMyEnquiries(Applicant applicant) {
        if (applicant == null || applicant.getId() == null) {
            return Collections.emptyList();
        }
        // return enquiryRepo.findByApplicantId(applicant.getId()); // Example repo call
        return findEnquiriesByApplicantPlaceholder(applicant.getId()); // Placeholder
        // Or return applicant.getEnquiries() if the list is maintained within the entity
    }

    /**
     * Retrieves a specific enquiry by ID, ensuring it belongs to the applicant.
     *
     * @param enquiryId The ID of the enquiry.
     * @param applicant The Applicant requesting the view.
     * @return The Enquiry object or null if not found/not owned.
     * @throws SecurityException if the enquiry does not belong to the applicant.
     */
    @Override
    public Enquiry viewMyEnquiryById(String enquiryId, Applicant applicant) throws SecurityException {
        if (enquiryId == null || applicant == null || applicant.getId() == null) {
            return null;
        }
        // Enquiry enquiry = enquiryRepo.findById(enquiryId); // Example repo call
        Enquiry enquiry = findEnquiryByIdPlaceholder(enquiryId); // Placeholder

        if (enquiry == null) {
            return null; // Not found
        }

        // Check ownership
        if (!enquiry.getApplicantId().equals(applicant.getId())) {
            throw new SecurityException("Applicant " + applicant.getId() + " cannot view enquiry " + enquiryId + " as they do not own it.");
        }

        return enquiry;
    }

    // --- IApplicantProjectView Implementation ---

    /**
     * Retrieves projects visible and eligible for the applicant.
     * (Note: This duplicates viewAvailableProjects, potentially consolidate later)
     * @param applicant The Applicant.
     * @return List of projects.
     */
    @Override
    public List<Project> viewVisibleEligibleProjects(Applicant applicant) {
        // This logic is identical to viewAvailableProjects in IApplicantService
        return viewAvailableProjects(applicant);
    }

    /**
     * Retrieves a specific project by ID if visible and potentially eligible for the applicant.
     *
     * @param projectId The ID of the project.
     * @param applicant The Applicant viewing.
     * @return The Project object or null.
     * @throws SecurityException If access denied.
     */
    @Override
    public Project viewApplicantProjectById(String projectId, Applicant applicant) throws SecurityException {
        if (projectId == null || applicant == null) return null;

        // Project project = projectRepo.findById(projectId); // Example repo call
        Project project = findProjectByIdPlaceholder(projectId); // Placeholder

        if (project == null) return null; // Not found

        // Check visibility
        if (!project.isVisible()) {
            // Allow viewing if they applied to it? Check application status.
            Application app = getApplicationStatus(applicant);
            if(app == null || !app.getProjectId().equals(projectId)){
                throw new SecurityException("Project " + projectId + " is not currently visible.");
            }
            // If they applied, allow viewing even if not visible anymore
        }


        // Check potential eligibility (eligible for at least one flat type)
        boolean eligibleForAnyFlat = false;
        if (project.getFlats() != null) {
            for (Flat flat : project.getFlats()) {
                if (checkEligibility(applicant, flat.getFlatType())) {
                    eligibleForAnyFlat = true;
                    break;
                }
            }
        }
        if (!eligibleForAnyFlat){
            // Allow viewing if they applied to it, even if eligibility changed? Check application.
            Application app = getApplicationStatus(applicant);
            if(app == null || !app.getProjectId().equals(projectId)){
                // Not eligible and didn't apply
                throw new SecurityException("Applicant is not eligible for any flat types in project " + projectId);
            }
            // If they applied, allow viewing even if no longer eligible
        }


        return project;
    }

    /**
     * Filters visible and eligible projects based on criteria.
     *
     * @param filters   Map of filter criteria (e.g., "neighbourhood").
     * @param applicant The Applicant performing the filter.
     * @return List of matching projects.
     */
    @Override
    public List<Project> filterApplicantProjects(Map<String, String> filters, Applicant applicant) {
        List<Project> eligibleProjects = viewVisibleEligibleProjects(applicant);
        if (filters == null || filters.isEmpty()) {
            return eligibleProjects; // No filters applied
        }

        List<Project> filteredList = new ArrayList<>();
        for (Project project : eligibleProjects) {
            boolean match = true;
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue();
                if (value == null || value.trim().isEmpty()) continue; // Skip empty filter values

                switch (key) {
                    case "neighbourhood":
                        if (project.getNeighbourhood() == null || !project.getNeighbourhood().equalsIgnoreCase(value)) {
                            match = false;
                        }
                        break;
                    case "flattype": // Check if project offers this flat type
                        try {
                            FlatType requestedType = FlatType.valueOf(value.toUpperCase());
                            boolean offersType = false;
                            if(project.getFlats() != null){
                                for(Flat flat : project.getFlats()){
                                    if(flat.getFlatType() == requestedType){
                                        offersType = true;
                                        break;
                                    }
                                }
                            }
                            if(!offersType) match = false;
                        } catch (IllegalArgumentException e) {
                            match = false; // Invalid flat type string
                        }
                        break;
                    // Add more filters as needed (e.g., project name contains)
                    case "projectname":
                        if (project.getProjName() == null || !project.getProjName().toLowerCase().contains(value.toLowerCase())) {
                            match = false;
                        }
                        break;
                    default:
                        // Ignore unknown filter keys
                        break;
                }
                if (!match) break; // Stop checking filters for this project if one fails
            }
            if (match) {
                filteredList.add(project);
            }
        }
        return filteredList;
    }


    //TODO remove and adjust
    // --- Placeholder methods for Repository Interactions ---
    // Replace these with actual calls to your repository implementation

    private List<Project> findAllProjectsPlaceholder() {
        // Simulate finding all projects
        List<Project> projects = new ArrayList<>();
        // Project 1 (Visible, Open)
        Project p1 = new Project("SkyVista @ Dawson", "Queenstown", new Date(System.currentTimeMillis() - 86400000 * 5), new Date(System.currentTimeMillis() + 86400000 * 10), null, null);
        p1.setProjectId("P1001");
        p1.setVisible(true);
        List<Flat> p1Flats = new ArrayList<>();
        p1Flats.add(new Flat(FlatType.TWOROOM, 50, 10, 150000));
        p1Flats.add(new Flat(FlatType.THREEROOM, 100, 20, 300000));
        p1.setFlats(p1Flats);
        projects.add(p1);

        // Project 2 (Visible, Closed)
        Project p2 = new Project("Greenery Grove", "Tampines", new Date(System.currentTimeMillis() - 86400000 * 30), new Date(System.currentTimeMillis() - 86400000 * 5), null, null);
        p2.setProjectId("P1002");
        p2.setVisible(true);
        List<Flat> p2Flats = new ArrayList<>();
        p2Flats.add(new Flat(FlatType.TWOROOM, 80, 5, 130000));
        p2.setFlats(p2Flats);
        projects.add(p2);

        // Project 3 (Hidden, Open)
        Project p3 = new Project("Woodlands Weave", "Woodlands", new Date(System.currentTimeMillis() - 86400000 * 2), new Date(System.currentTimeMillis() + 86400000 * 15), null, null);
        p3.setProjectId("P1003");
        p3.setVisible(false);
        List<Flat> p3Flats = new ArrayList<>();
        p3Flats.add(new Flat(FlatType.THREEROOM, 120, 30, 280000));
        p3.setFlats(p3Flats);
        projects.add(p3);

        return projects;
    }

    private Application findActiveApplicationByApplicantPlaceholder(String applicantId) {
        // Simulate finding an active application (PENDING, SUCCESSFUL, WITHDRAW_PENDING, BOOKED?)
        // This user has no active app
        if ("S1234567A".equalsIgnoreCase(applicantId)) return null;
        // This user might have one - check status
        // Return null for now
        return null;
    }
    private Application findApplicationByIdPlaceholder(String appId) {
        // Simulate finding an application by ID
        // Return null for now
        return null;
    }

    private Project findProjectByIdPlaceholder(String projectId) {
        for(Project p : findAllProjectsPlaceholder()){
            if(p.getProjectId().equals(projectId)) return p;
        }
        return null;
    }


    private void saveApplicationPlaceholder(Application app) throws Exception {
        System.out.println("Placeholder: Saving application ID " + app.getId());
    }

    private Enquiry findEnquiryByIdPlaceholder(String enquiryId) {
        // Simulate finding an enquiry
        if ("E9001".equals(enquiryId)){
            return new Enquiry(enquiryId, "S1234567A", "P1001", "When is the completion date?", null);
        }
        return null;
    }

    private List<Enquiry> findEnquiriesByApplicantPlaceholder(String applicantId) {
        List<Enquiry> enquiries = new ArrayList<>();
        if ("S1234567A".equalsIgnoreCase(applicantId)) {
            enquiries.add(new Enquiry("E9001", applicantId, "P1001", "When is the completion date?", null));
            enquiries.add(new Enquiry("E9002", applicantId, "P1001", "Can I choose my unit?", "Unit selection is later."));
        }
        return enquiries;
    }

    private void saveEnquiryPlaceholder(Enquiry enquiry) throws Exception {
        System.out.println("Placeholder: Saving enquiry ID " + enquiry.getEnquiryId());
    }

    private void deleteEnquiryPlaceholder(String enquiryId) throws Exception {
        System.out.println("Placeholder: Deleting enquiry ID " + enquiryId);
    }

}