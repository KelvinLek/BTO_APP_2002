package service;

import entity.*;
import pub_enums.*;
import java.util.*;
// Import necessary repository interfaces if interacting with data storage
// import repository.*;

/**
 * Provides services specific to HDB Officers, including project registration,
 * viewing assigned projects/enquiries, replying to enquiries, and booking flats.
 * Inherits Applicant capabilities.
 */
public class HdbOfficerService extends ApplicantService implements IBookFlatService, IEnquiryViewable, IHdbOfficerService, IOfficerProjectView, IOfficerEnquiryView, IReplyable {

    // Assume repository injection
    // private final IHdbOfficerRepo officerRepo; // Specific repo for officer data/status
    // private final IReceiptService receiptService; // May need receipt service for generation

    // Constructor example (adjust based on actual dependencies)
    // public HdbOfficerService(IUserRepo userRepo, IApplicantRepo applicantRepo, IApplicationRepo applicationRepo,
    //                          IProjectRepo projectRepo, IEnquiryRepo enquiryRepo, IHdbOfficerRepo officerRepo,
    //                          IReceiptService receiptService) {
    //     super(userRepo, applicantRepo, applicationRepo, projectRepo, enquiryRepo); // Call parent constructor
    //     this.officerRepo = officerRepo;
    //     this.receiptService = receiptService;
    // }


    // --- IHdbOfficerService Implementation ---

    /**
     * Registers an HDB Officer's interest for a project. Sets status to Pending.
     * Checks eligibility (no overlap, hasn't applied as applicant).
     *
     * @param officer   The HdbOfficer registering.
     * @param project   The Project to register for.
     * @return true if registration request logged successfully.
     * @throws NoSuchElementException   If officer/project not found.
     * @throws IllegalStateException    If ineligible.
     * @throws Exception                For persistence errors.
     */
    @Override
    public boolean registerForProject(HdbOfficer officer, Project project) throws NoSuchElementException, IllegalStateException, Exception {
        if (officer == null || project == null) {
            throw new NoSuchElementException("Officer and Project cannot be null.");
        }
        if (officer.getId() == null || project.getProjectId() == null){
            throw new NoSuchElementException("Officer or Project ID missing.");
        }

        // 1. Check if Officer has applied for this project as an Applicant
        Application existingApp = findActiveApplicationByApplicantPlaceholder(officer.getId()); // Placeholder
        if(existingApp != null && existingApp.getProjectId().equals(project.getProjectId())){
            throw new IllegalStateException("Officer cannot register for a project they have applied to as an applicant.");
        }

        // 2. Check for overlapping duty period with currently *approved* assigned projects
        List<Project> assignedProjects = officer.getAssignedProjects(); // Assumes this list holds approved assignments
        if(assignedProjects != null && project.getAppOpen() != null && project.getAppClose() != null){
            for (Project assigned : assignedProjects) {
                if (assigned.getAppOpen() != null && assigned.getAppClose() != null) {
                    // Check for overlap: (StartA <= EndB) and (EndA >= StartB)
                    if (!project.getAppOpen().after(assigned.getAppClose()) && !project.getAppClose().before(assigned.getAppOpen())) {
                        throw new IllegalStateException("Officer already assigned to project '" + assigned.getProjName() + "' during this period.");
                    }
                }
            }
        }


        // 3. Update officer's status or log request (Implementation depends on how pending requests are tracked)
        // Option A: Set a generic status on the officer object
        officer.setStatus("PENDING_REGISTRATION_" + project.getProjectId()); // Example status

        // Option B: Create a separate RegistrationRequest entity/record (more robust)
        // RegistrationRequest request = new RegistrationRequest(officer.getId(), project.getId(), "PENDING");
        // registrationRequestRepo.save(request); // Example

        try {
            // Save the updated officer status or the new request
            // officerRepo.save(officer); // Example for Option A
            saveUserPlaceholder(officer); // Placeholder
            System.out.println("Officer " + officer.getId() + " registration request submitted for project " + project.getProjectId());
            return true;
        } catch (Exception e) {
            System.err.println("Error saving officer registration request: " + e.getMessage());
            throw new Exception("Failed to log registration request.", e);
        }
    }

    /**
     * Retrieves the registration status for an officer and project.
     *
     * @param officer The HdbOfficer.
     * @param project The Project.
     * @return String representing the status (e.g., "Pending Registration", "Approved", "Rejected", "Not Registered").
     * @throws NoSuchElementException if officer or project not found.
     */
    @Override
    public String viewRegistrationStatus(HdbOfficer officer, Project project) throws NoSuchElementException {
        if (officer == null || project == null || officer.getId() == null || project.getProjectId() == null) {
            throw new NoSuchElementException("Officer or Project missing for status check.");
        }

        // Logic depends on how status is stored.
        // Option A: Check officer's general status field
        String currentStatus = officer.getStatus();
        String pendingMarker = "PENDING_REGISTRATION_" + project.getProjectId();
        if (currentStatus != null && currentStatus.startsWith(pendingMarker)) {
            return "Pending Registration";
        }

        // Option B: Check a dedicated RegistrationRequest entity
        // RegistrationRequest request = registrationRequestRepo.findByOfficerAndProject(officer.getId(), project.getId());
        // if (request != null) return request.getStatus(); // e.g., "PENDING", "APPROVED", "REJECTED"

        // Option C: Check if the project is in the officer's assignedProjects list (implies approved)
        List<Project> assigned = officer.getAssignedProjects();
        if (assigned != null) {
            for(Project p : assigned){
                if(p.getProjectId().equals(project.getProjectId())){
                    return "Approved"; // Found in assigned list
                }
            }
        }

        // If none of the above, assume not registered or rejected without specific status
        // Check officer status for generic rejection?
        if("Rejected".equals(currentStatus)) { // Vague if multiple rejections possible
            // Need more specific status tracking
        }

        return "Not Registered"; // Default if no specific status found
    }


    // --- IBookFlatService Implementation ---

    /**
     * Books a flat for an applicant with a successful application.
     * Updates application status to Booked, decrements project flat count.
     *
     * @param application The successful Application.
     * @param flatType    The FlatType being booked.
     * @param officer     The HdbOfficer performing the booking.
     * @return true if booking successful.
     * @throws NoSuchElementException   If application/project/officer not found.
     * @throws IllegalStateException    If application status isn't Successful or no units available.
     * @throws SecurityException        If officer not assigned/approved for the project.
     * @throws Exception                For persistence errors.
     */
    @Override
    public boolean bookFlat(Application application, FlatType flatType, HdbOfficer officer) throws NoSuchElementException, IllegalStateException, SecurityException, Exception {
        if (application == null || flatType == null || officer == null || application.getId() == null || application.getProjectId() == null || officer.getId() == null) {
            throw new IllegalArgumentException("Application, FlatType, and Officer details are required.");
        }

        // 1. Verify officer is assigned and approved for this project
        Project project = findProjectByIdPlaceholder(application.getProjectId()); // Placeholder
        if(project == null){
            throw new NoSuchElementException("Project " + application.getProjectId() + " not found.");
        }
        boolean isAssigned = false;
        if(officer.getAssignedProjects() != null){
            for(Project p : officer.getAssignedProjects()){
                if(p.getProjectId().equals(project.getProjectId())){
                    isAssigned = true;
                    break;
                }
            }
        }
        if(!isAssigned || !"Approved".equals(viewRegistrationStatus(officer, project)) /* More robust status check */) {
            throw new SecurityException("Officer " + officer.getId() + " is not authorized to book flats for project " + project.getProjectId());
        }

        // 2. Find the application again to work with the persisted state
        Application appToBook = findApplicationByIdPlaceholder(application.getId()); // Placeholder
        if(appToBook == null) {
            throw new NoSuchElementException("Application with ID " + application.getId() + " not found.");
        }


        // 3. Check application status is "Successful"
        if (appToBook.getStatus() != ApplStatus.SUCCESS) {
            throw new IllegalStateException("Application status must be SUCCESSFUL to book a flat. Current status: " + appToBook.getStatus());
        }

        // 4. Check flat availability and decrement count
        boolean booked = false;
        if (project.getFlats() != null) {
            for (Flat flat : project.getFlats()) {
                if (flat.getFlatType() == flatType) {
                    if (flat.getRemaining() > 0) {
                        flat.setRemaining(flat.getRemaining() - 1); // Decrement
                        booked = true;
                    } else {
                        throw new IllegalStateException("No remaining units available for flat type: " + flatType + " in project " + project.getProjName());
                    }
                    break; // Found the flat type
                }
            }
        }
        if (!booked) {
            // This case implies the flatType exists but wasn't found in the loop (error)
            // OR the project has no flats defined matching the type.
            throw new IllegalStateException("Selected flat type " + flatType + " not found or could not be booked in project " + project.getProjName());
        }


        // 5. Update application status to "Booked"
        appToBook.setStatus(ApplStatus.BOOKED);
        // Optionally update applicant's main status if needed:
        // Applicant applicant = findUserByIdPlaceholder(appToBook.getApplicantId());
        // if (applicant instanceof Applicant) { ((Applicant)applicant).setStatus("Booked"); saveUserPlaceholder(applicant);}


        // 6. Save changes (atomicity needed in real systems)
        try {
            saveApplicationPlaceholder(appToBook); // Save application status
            saveProjectPlaceholder(project); // Save updated flat counts
            System.out.println("Flat " + flatType + " booked successfully for application " + appToBook.getId());
            // Generate receipt (could be done here or separately)
            // generateReceipt(appToBook, officer); // Requires IReceiptService
            return true;
        } catch (Exception e) {
            System.err.println("Error saving booking: " + e.getMessage());
            // Rollback changes if possible (decrementing flat count, status update)
            // flat.setRemaining(flat.getRemaining() + 1); // Example rollback
            // appToBook.setStatus(ApplStatus.SUCCESS);
            throw new Exception("Failed to finalize booking.", e);
        }
    }


    // --- IEnquiryViewable Implementation ---
    // Provides generic viewing capabilities, potentially overlapping with role-specific views

    /**
     * Retrieves all enquiries for a specific project.
     * @param projectId The project ID.
     * @return List of enquiries.
     */
    @Override
    public List<Enquiry> viewEnquiriesByProject(UUID projectId) {
        if(projectId == null) return Collections.emptyList();
        return findEnquiriesByProjectPlaceholder(projectId.toString()); // Placeholder
    }

    /**
     * Retrieves all enquiries submitted by a specific applicant.
     * @param applicantId The applicant's ID.
     * @return List of enquiries.
     */
    @Override
    public List<Enquiry> viewEnquiriesByApplicant(String applicantId) {
        if(applicantId == null) return Collections.emptyList();
        return findEnquiriesByApplicantPlaceholder(applicantId); // Placeholder
    }

    /**
     * Retrieves all enquiries (Use with caution).
     * @return List of all enquiries.
     */
    @Override
    public List<Enquiry> viewAllEnquiries() {
        return findAllEnquiriesPlaceholder(); // Placeholder
    }

    /**
     * Retrieves a single enquiry by ID.
     * @param enquiryId The enquiry ID.
     * @return The Enquiry or null.
     */
    @Override
    public Enquiry viewEnquiryById(String enquiryId) {
        if(enquiryId == null) return null;
        return findEnquiryByIdPlaceholder(enquiryId); // Placeholder
    }


    // --- IOfficerProjectView Implementation ---

    /**
     * Retrieves details of a project the officer is assigned to.
     * @param projectId The project ID.
     * @param officer   The HdbOfficer viewing.
     * @return The Project or null.
     * @throws SecurityException If officer not assigned.
     */
    @Override
    public Project viewAssignedProjectDetails(String projectId, HdbOfficer officer) throws SecurityException {
        if (projectId == null || officer == null || officer.getId() == null) return null;

        Project project = findProjectByIdPlaceholder(projectId); // Placeholder
        if(project == null) return null; // Not found

        // Verify assignment
        boolean isAssigned = false;
        if(officer.getAssignedProjects() != null){
            for(Project p : officer.getAssignedProjects()){
                if(p.getProjectId().equals(project.getProjectId())){
                    isAssigned = true;
                    break;
                }
            }
        }
        if(!isAssigned) {
            throw new SecurityException("Officer " + officer.getId() + " is not assigned to project " + projectId);
        }

        return project;
    }

    /**
     * Retrieves the list of projects the officer is assigned to.
     * @param officer The HdbOfficer.
     * @return List of assigned Projects.
     */
    @Override
    public List<Project> viewMyAssignedProjects(HdbOfficer officer) {
        if (officer == null) return Collections.emptyList();
        // This assumes the HdbOfficer entity correctly maintains the list of assigned projects.
        List<Project> assigned = officer.getAssignedProjects();
        return (assigned != null) ? assigned : Collections.emptyList();
    }


    // --- IOfficerEnquiryView Implementation ---

    /**
     * Retrieves enquiries for a project the officer is assigned to.
     * @param projectId The project ID.
     * @param officer   The HdbOfficer viewing.
     * @return List of enquiries.
     * @throws SecurityException If officer not assigned.
     */
    @Override
    public List<Enquiry> viewEnquiriesByAssignedProject(String projectId, HdbOfficer officer) throws SecurityException {
        if (projectId == null || officer == null || officer.getId() == null) return Collections.emptyList();

        // Verify assignment first
        viewAssignedProjectDetails(projectId, officer); // This throws SecurityException if not assigned

        // If assigned, fetch enquiries for that project
        return findEnquiriesByProjectPlaceholder(projectId); // Placeholder
    }

    /**
     * Retrieves a single enquiry by ID if it belongs to an assigned project.
     * @param enquiryId The enquiry ID.
     * @param officer   The HdbOfficer viewing.
     * @return The Enquiry or null.
     * @throws SecurityException If officer cannot view this enquiry.
     */
    @Override
    public Enquiry viewAssignedEnquiryById(String enquiryId, HdbOfficer officer) throws SecurityException {
        if (enquiryId == null || officer == null || officer.getId() == null) return null;

        Enquiry enquiry = findEnquiryByIdPlaceholder(enquiryId); // Placeholder
        if (enquiry == null) return null; // Not found

        // Check if the enquiry's project is one the officer is assigned to
        try {
            viewAssignedProjectDetails(enquiry.getProjectId(), officer); // Re-use assignment check
            // If the above doesn't throw SecurityException, the officer is assigned
            return enquiry;
        } catch (SecurityException e) {
            // Officer is not assigned to the project this enquiry belongs to
            throw new SecurityException("Officer " + officer.getId() + " cannot view enquiry " + enquiryId + " (not assigned to project " + enquiry.getProjectId() + ").");
        }
    }


    // --- IReplyable Implementation ---

    /**
     * Adds a reply to an enquiry. Checks if officer is assigned to the project.
     * @param enquiry      The Enquiry to reply to.
     * @param replyText    The reply message.
     * @param replyingUser The User replying (must be HdbOfficer or HdbManager).
     * @return true if reply successful.
     * @throws NoSuchElementException If enquiry not found.
     * @throws SecurityException      If user lacks permission.
     * @throws Exception              For persistence errors.
     */
    @Override
    public boolean replyToEnquiry(Enquiry enquiry, String replyText, User replyingUser) throws NoSuchElementException, SecurityException, Exception {
        if (enquiry == null || enquiry.getEnquiryId() == null || replyText == null || replyText.trim().isEmpty() || replyingUser == null || replyingUser.getId() == null) {
            throw new IllegalArgumentException("Enquiry, reply text, and replying user details are required.");
        }

        // 1. Find the existing enquiry
        Enquiry existingEnquiry = findEnquiryByIdPlaceholder(enquiry.getEnquiryId()); // Placeholder
        if (existingEnquiry == null) {
            throw new NoSuchElementException("Enquiry with ID " + enquiry.getEnquiryId() + " not found.");
        }

        // 2. Check permission: User must be an HDB Officer assigned to the project OR an HDB Manager
        boolean authorized = false;
        if (replyingUser.getRole() == Role.HDBMANAGER) {
            authorized = true; // Managers can reply to any enquiry
        } else if (replyingUser instanceof HdbOfficer) {
            HdbOfficer officer = (HdbOfficer) replyingUser;
            try {
                // Check if officer is assigned to the enquiry's project
                viewAssignedProjectDetails(existingEnquiry.getProjectId(), officer);
                authorized = true;
            } catch (SecurityException e) {
                authorized = false; // Officer not assigned
            }
        }

        if (!authorized) {
            throw new SecurityException("User " + replyingUser.getId() + " (Role: " + replyingUser.getRole() + ") is not authorized to reply to enquiry " + existingEnquiry.getEnquiryId());
        }


        // 3. Update the reply
        existingEnquiry.setReply(replyText + " [Replied by: " + replyingUser.getId() + "]"); // Add who replied


        // 4. Save the updated enquiry
        try {
            saveEnquiryPlaceholder(existingEnquiry); // Placeholder
            System.out.println("Reply added to enquiry " + existingEnquiry.getEnquiryId() + " by " + replyingUser.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error saving enquiry reply: " + e.getMessage());
            // existingEnquiry.setReply(originalReply); // Rollback
            throw new Exception("Failed to save enquiry reply.", e);
        }
    }

//TODO REMOVE
    // --- Additional Placeholder methods for Repository Interactions ---

    private List<Enquiry> findEnquiriesByProjectPlaceholder(String projectId) {
        List<Enquiry> enquiries = new ArrayList<>();
        if ("P1001".equals(projectId)){
            enquiries.add(new Enquiry("E9001", "S1234567A", projectId, "When is the completion date?", null));
            enquiries.add(new Enquiry("E9002", "S1234567A", projectId, "Can I choose my unit?", "Unit selection is later."));
        } else if ("P1003".equals(projectId)){
            enquiries.add(new Enquiry("E9003", "S9999999Z", projectId, "Is parking included?", null));
        }
        return enquiries;
    }

    private List<Enquiry> findAllEnquiriesPlaceholder() {
        List<Enquiry> all = new ArrayList<>();
        all.addAll(findEnquiriesByProjectPlaceholder("P1001"));
        all.addAll(findEnquiriesByProjectPlaceholder("P1003"));
        // Add more from other simulated projects if needed
        return all;
    }


    private void saveProjectPlaceholder(Project project) throws Exception {
        System.out.println("Placeholder: Saving project ID " + project.getProjectId());
    }

    // Inherits other placeholders from ApplicantService via extension

}