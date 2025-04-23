package service;

import entity.*;
import pub_enums.*;
import repository.*;

import java.util.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

/**
 * Provides services specific to BTO Applicants, handling project viewing,
 * applications, status checks, withdrawals, enquiries, and eligibility.
 */
public class ApplicantService extends UserService implements IApplyableService, IEligibilityCheck, IProjectView {

    private ApplicantRepo applicantRepo;
    private ProjectRepo projectRepo;
    private ApplicationRepo applicationRepo;
    private EnquiryRepo enquiryRepo;

    public ApplicantService(ApplicantRepo applicantRepo, ProjectRepo projectRepo, 
                            ApplicationRepo applicationRepo, EnquiryRepo enquiryRepo) {
        super();
        this.applicantRepo = applicantRepo;
        this.projectRepo = projectRepo;
        this.applicationRepo = applicationRepo;
        this.enquiryRepo = enquiryRepo;
    }

    // --- IProjectView Implementation ---

    /**
     * Retrieves the details of a project by its ID if visible and eligible for the applicant.
     *
     * @param projectId The ID of the project.
     * @param user The User viewing the project.
     * @return The Project object or null.
     */
    @Override
    public Project viewProjectById(String projectId, User user) {
        if (projectId == null || user == null || !(user instanceof Applicant)) return null;
        Applicant applicant = (Applicant) user;

        Project project = projectRepo.findById(projectId).orElse(null);
        if (project == null) return null;

        // Check visibility
        if (!project.isVisible()) {
            // Allow viewing if they applied to it
            Application app = getApplicationStatus(applicant);
            if(app == null || !app.getProjectId().equals(projectId)){
                return null; // Not visible and not applied
            }
        }

        // Check potential eligibility (eligible for at least one flat type)
        boolean eligibleForAnyFlat = checkEligibility(user, project);
        if (!eligibleForAnyFlat){
            // Allow viewing if they applied to it, even if eligibility changed
            Application app = getApplicationStatus(applicant);
            if(app == null || !app.getProjectId().equals(projectId)){
                return null; // Not eligible and didn't apply
            }
        }

        return project;
    }

    /**
     * Filters visible and eligible projects based on criteria.
     *
     * @param filters Map of filter criteria (e.g., "neighbourhood").
     * @param user The User performing the filter.
     * @return List of matching projects.
     */
    @Override
    public List<Project> filterAllProjects(Map<String, String> filters, User user) {
        if (!(user instanceof Applicant)) return Collections.emptyList();
        Applicant applicant = (Applicant) user;

        List<Project> eligibleProjects = viewProjectsByUser(user);
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

    /**
     * Retrieves projects visible and eligible for the applicant.
     *
     * @param user The User viewing the projects.
     * @return List of projects.
     */
    @Override
    public List<Project> viewProjectsByUser(User user) {
        if (!(user instanceof Applicant)) return Collections.emptyList();
        Applicant applicant = (Applicant) user;

        List<Project> allProjects = projectRepo.findAll();
        List<Project> availableProjects = new ArrayList<>();
        Date today = new Date(); // Use current date for checking application period

        for (Project project : allProjects) {
            // Check project visibility and application period
            if (project.isVisible() && project.getAppOpen() != null && project.getAppClose() != null &&
                    !today.before(project.getAppOpen()) && !today.after(project.getAppClose())) {

                // Check if applicant is eligible for any flat type in this project
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

    // --- IApplyableService Implementation ---

    /**
     * Calculates the age of the user based on Date of Birth.
     * @param birthDate The user's Date of Birth.
     * @return The age in years, or 0 if birthDate is null.
     */
    @Override
    public int calculateAge(Date birthDate) {
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
     * Submits a BTO application for the default flat type after performing eligibility checks.
     *
     * @param user The User applying.
     * @param project The Project being applied for.
     * @return true if the application was submitted successfully.
     */
    @Override
    public boolean applyForProject(User user, Project project) {
        if (!(user instanceof Applicant) || project == null) {
            return false;
        }
        
        Applicant applicant = (Applicant) user;
        
        try {
            // Find the first eligible flat type
            FlatType eligibleFlatType = null;
            if (project.getFlats() != null) {
                for (Flat flat : project.getFlats()) {
                    if (checkEligibility(applicant, flat.getFlatType())) {
                        eligibleFlatType = flat.getFlatType();
                        break;
                    }
                }
            }
            
            if (eligibleFlatType == null) {
                return false; // No eligible flat type found
            }
            
            // Check if project is open for application
            Date today = new Date();
            if (project.getAppOpen() == null || project.getAppClose() == null ||
                    today.before(project.getAppOpen()) || today.after(project.getAppClose()) || !project.isVisible()) {
                return false; // Project not open
            }

            // Check if applicant already has an active application
            Application existingApp = applicationRepo.findActiveByApplicantId(applicant.getId());
            if (existingApp != null) {
                return false; // Already has active application
            }

            // Create and save the new application
            Application newApplication = new Application(
                    UUID.randomUUID().toString(),
                    ApplStatus.PENDING,
                    applicant.getId(),
                    project.getProjectId(),
                    eligibleFlatType.name()
            );

            applicationRepo.add(newApplication);
            
            // Associate application with applicant
            applicant.setApplication(newApplication);
            applicantRepo.update(applicant);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error during project application: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current application status for the user.
     *
     * @param user The User.
     * @return The current Application object, or null if none found.
     */
    @Override
    public Application getApplicationStatus(User user) {
        if (!(user instanceof Applicant)) return null;
        Applicant applicant = (Applicant) user;
        
        // First check if application is stored in applicant object
        if (applicant.getApplication() != null) {
            return applicant.getApplication();
        }
        
        // Otherwise try to find it in the repository
        return applicationRepo.findActiveByApplicantId(applicant.getId());
    }

    /**
     * Submits a new enquiry for a project.
     *
     * @param user The User submitting the enquiry.
     * @param project The Project the enquiry is about.
     * @param message The enquiry message.
     * @return The created Enquiry object.
     */
    @Override
    public Enquiry submitEnquiry(User user, Project project, String message) {
        if (!(user instanceof Applicant) || project == null || message == null || message.trim().isEmpty()) {
            return null;
        }
        
        Applicant applicant = (Applicant) user;
        
        try {
            // Create new Enquiry object
            Enquiry newEnquiry = new Enquiry(
                    UUID.randomUUID().toString(),
                    applicant.getId(),
                    project.getProjectId(),
                    message,
                    null // No reply initially
            );

            enquiryRepo.add(newEnquiry);
            
            // Add enquiry to applicant's list
            applicant.addEnquiry(newEnquiry);
            applicantRepo.update(applicant);
            
            return newEnquiry;
        } catch (Exception e) {
            System.err.println("Error submitting enquiry: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all enquiries submitted by a user.
     *
     * @param user The User.
     * @return A List of Enquiry objects.
     */
    @Override
    public List<Enquiry> viewEnquiries(User user) {
        if (!(user instanceof Applicant)) return Collections.emptyList();
        Applicant applicant = (Applicant) user;
        
        // First check if enquiries are stored in applicant object
        if (applicant.getEnquiries() != null && !applicant.getEnquiries().isEmpty()) {
            return applicant.getEnquiries();
        }
        
        // Otherwise try to find them in the repository
        return enquiryRepo.findByApplicantId(applicant.getId());
    }

    /**
     * Retrieves a specific enquiry by ID, ensuring it belongs to the user.
     *
     * @param enquiryId The ID of the enquiry.
     * @param user The User requesting the view.
     * @return The Enquiry object or null if not found/not owned.
     */
    @Override
    public Enquiry viewEnquiryById(String enquiryId, User user) {
        if (!(user instanceof Applicant) || enquiryId == null) return null;
        Applicant applicant = (Applicant) user;
        
        Enquiry enquiry = enquiryRepo.findById(enquiryId).orElse(null);
        
        if (enquiry == null) {
            return null; // Not found
        }
        
        // Check ownership
        if (!enquiry.getApplicantId().equals(applicant.getId())) {
            return null; // Not authorized to view
        }
        
        return enquiry;
    }

    /**
     * Edits an existing enquiry's message. Only the owner can edit.
     *
     * @param enquiryId The ID of the enquiry to edit.
     * @param user The User attempting to edit.
     * @param newMessage The new message.
     */
    @Override
    public void editEnquiry(String enquiryId, User user, String newMessage) {
        if (!(user instanceof Applicant) || enquiryId == null || newMessage == null || newMessage.trim().isEmpty()) {
            return;
        }
        
        Applicant applicant = (Applicant) user;
        Enquiry enquiry = enquiryRepo.findById(enquiryId).orElse(null);
        
        if (enquiry == null) {
            return; // Enquiry not found
        }
        
        // Check ownership
        if (!enquiry.getApplicantId().equals(applicant.getId())) {
            return; // Not authorized to edit
        }
        
        // Update the message
        enquiry.setMessage(newMessage);
        enquiryRepo.update(enquiry);
        
        // Also update in applicant's list if present
        if (applicant.getEnquiries() != null) {
            for (Enquiry e : applicant.getEnquiries()) {
                if (e.getEnquiryId().equals(enquiryId)) {
                    e.setMessage(newMessage);
                    break;
                }
            }
            applicantRepo.update(applicant);
        }
    }

    /**
     * Deletes an existing enquiry. Only the owner can delete.
     *
     * @param enquiryId The ID of the enquiry to delete.
     * @param user The User attempting to delete.
     */
    @Override
    public void deleteEnquiry(String enquiryId, User user) {
        if (!(user instanceof Applicant) || enquiryId == null) {
            return;
        }
        
        Applicant applicant = (Applicant) user;
        Enquiry enquiry = enquiryRepo.findById(enquiryId).orElse(null);
        
        if (enquiry == null) {
            return; // Enquiry not found
        }
        
        // Check ownership
        if (!enquiry.getApplicantId().equals(applicant.getId())) {
            return; // Not authorized to delete
        }
        
        // Delete from repository
        enquiryRepo.delete(enquiryId);
        
        // Also remove from applicant's list if present
        if (applicant.getEnquiries() != null) {
            applicant.getEnquiries().removeIf(e -> e.getEnquiryId().equals(enquiryId));
            applicantRepo.update(applicant);
        }
    }

    // --- IEligibilityCheck Implementation ---

    /**
     * Checks if a user is eligible for a specific flat type based on age and marital status.
     * Rules: Single >= 35 (2-Room only), Married >= 21 (Any).
     *
     * @param user The User.
     * @param flatType The FlatType being considered.
     * @return true if eligible, false otherwise.
     */
    @Override
    public boolean checkEligibility(User user, FlatType flatType) {
        if (!(user instanceof Applicant)) return false;
        Applicant applicant = (Applicant) user;
        
        if (applicant.getDob() == null || applicant.getMaritalStatus() == null) {
            return false;
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
     * Checks if a user is eligible to apply for a *project* (eligible for any offered flat type)
     * and doesn't have an active application.
     *
     * @param user The User.
     * @param project The Project.
     * @return true if eligible to apply, false otherwise.
     */
    @Override
    public boolean checkEligibility(User user, Project project) {
        if (!(user instanceof Applicant)) return false;
        Applicant applicant = (Applicant) user;
        
        if (project.getFlats() == null || project.getFlats().isEmpty()) {
            return false;
        }

        // Check if eligible for at least one flat type offered by the project
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

        // Check if applicant already has an active application
        Application existingApp = applicationRepo.findActiveByApplicantId(applicant.getId());
        if (existingApp != null) {
            return false; // Already has an active application
        }

        return true; // Eligible for a flat and no active application
    }

    /**
     * Requests withdrawal for the specified application. Sets status to WITHDRAW_PENDING.
     *
     * @param application The Application to withdraw.
     * @return true if the request was logged successfully.
     */
    public boolean requestWithdrawal(Application application) {
        if (application == null || application.getId() == null) {
            return false;
        }

        // Find the application to ensure it exists and check its current state
        Application appToWithdraw = applicationRepo.findById(application.getId()).orElse(null);
        if (appToWithdraw == null) {
            return false;
        }

        // Check if withdrawal is allowed
        ApplStatus currentStatus = appToWithdraw.getStatus();
        if (currentStatus == ApplStatus.WITHDRAW_APPROVED || currentStatus == ApplStatus.REJECT) {
            return false; // Already in final state
        }
        if (currentStatus == ApplStatus.WITHDRAW_PENDING) {
            return true; // Already requested
        }

        // Update status to pending withdrawal
        appToWithdraw.setStatus(ApplStatus.WITHDRAW_PENDING);
        applicationRepo.update(appToWithdraw);
        
        // Update in applicant's object if present
        String applicantId = appToWithdraw.getApplicantId();
        Optional<Applicant> optApplicant = applicantRepo.findById(applicantId);
        if (optApplicant.isPresent()) {
            Applicant applicant = optApplicant.get();
            if (applicant.getApplication() != null && 
                applicant.getApplication().getId().equals(appToWithdraw.getId())) {
                applicant.setApplication(appToWithdraw);
                applicantRepo.update(applicant);
            }
        }
        
        return true;
    }
}