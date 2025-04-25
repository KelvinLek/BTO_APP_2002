package service;

import entity.*;
import pub_enums.*;
import repository.*;

import java.util.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

/**
 * Provides services specific to HDB Officers, handling project viewing,
 * application processing, and enquiry handling.
 */
public class HdbOfficerService extends UserService implements IApplyableService, IEligibilityCheck, IProjectView {

    private HdbOfficerRepo officerRepo;
    private ProjectRepo projectRepo;
    private ApplicationRepo applicationRepo;
    private EnquiryRepo enquiryRepo;
    private ApplicantRepo applicantRepo;

    public HdbOfficerService(HdbOfficerRepo officerRepo, ProjectRepo projectRepo, 
                            ApplicationRepo applicationRepo, EnquiryRepo enquiryRepo,
                            ApplicantRepo applicantRepo) {
        super();
        this.officerRepo = officerRepo;
        this.projectRepo = projectRepo;
        this.applicationRepo = applicationRepo;
        this.enquiryRepo = enquiryRepo;
        this.applicantRepo = applicantRepo;
    }

    // --- IProjectView Implementation ---

    /**
     * Retrieves the details of a project by its ID if the officer is assigned to it.
     *
     * @param projectId The ID of the project.
     * @param user The User viewing the project.
     * @return The Project object or null.
     */
    @Override
    public Project viewProjectById(String projectId, User user) {
        if (projectId == null || user == null || !(user instanceof HdbOfficer)) return null;
        HdbOfficer officer = (HdbOfficer) user;

        Project project = projectRepo.findById(projectId).orElse(null);
        if (project == null) return null;

        // Check if the officer is assigned to this project
        if (project.getOfficers() != null && project.getOfficers().stream()
                .anyMatch(o -> o.getId().equals(officer.getId()))) {
            return project;
        }
        
        // Officers can also view projects they're not assigned to if they're visible
        if (project.isVisible()) {
            return project;
        }
        
        return null; // Not assigned and not visible
    }

    /**
     * Filters projects based on criteria.
     *
     * @param filters Map of filter criteria.
     * @param user The User performing the filter.
     * @return List of matching projects.
     */
    @Override
    public List<Project> filterAllProjects(Map<String, String> filters, User user) {
        if (!(user instanceof HdbOfficer)) return Collections.emptyList();
        
        List<Project> accessibleProjects = viewProjectsByUser(user);
        if (filters == null || filters.isEmpty()) {
            return accessibleProjects; // No filters applied
        }

        List<Project> filteredList = new ArrayList<>();
        for (Project project : accessibleProjects) {
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
                    case "assigned":
                        boolean assigned = Boolean.parseBoolean(value);
                        boolean isAssigned = isOfficerAssigned((HdbOfficer)user, project);
                        if (assigned != isAssigned) {
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
     * Retrieves projects that the officer is assigned to.
     *
     * @param user The User viewing the projects.
     * @return List of projects.
     */
    @Override
    public List<Project> viewProjectsByUser(User user) {
        if (!(user instanceof HdbOfficer)) return Collections.emptyList();
        HdbOfficer officer = (HdbOfficer) user;

        // Get all projects
        List<Project> allProjects = projectRepo.findAll();
        List<Project> assignedProjects = new ArrayList<>();
        List<Project> visibleProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // Check if officer is assigned to this project
            if (project.getOfficers() != null && project.getOfficers().stream()
                    .anyMatch(o -> o.getId().equals(officer.getId()) && o.getStatus() == OfficerStatus.ASSIGNED)) {
                assignedProjects.add(project);
            }
            // Also include visible projects
            else if (project.isVisible()) {
                visibleProjects.add(project);
            }
        }
        
        // Combine lists with assigned projects first
        List<Project> result = new ArrayList<>(assignedProjects);
        result.addAll(visibleProjects);
        
        return result;
    }
    
    /**
     * Checks if an officer is assigned to a project.
     */
    private boolean isOfficerAssigned(HdbOfficer officer, Project project) {
        if (project.getOfficers() == null) return false;
        
        return project.getOfficers().stream()
                .anyMatch(o -> o.getId().equals(officer.getId()));
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
     * Officers cannot apply for projects. This method always returns false.
     */
    @Override
    public boolean applyForProject(User user, Project project) {
        // Officers cannot apply for projects
        return false;
    }

    /**
     * Retrieves application status. Not applicable for officers.
     */
    @Override
    public Application getApplicationStatus(User user) {
        // Not applicable for officers
        return null;
    }

    /**
     * Submits a new enquiry response. Officers can reply to enquiries.
     *
     * @param user The Officer responding.
     * @param project The Project the enquiry is about.
     * @param message The reply message.
     * @return The updated Enquiry object.
     */
    @Override
    public Enquiry submitEnquiry(User user, Project project, String message) {
        // Officers use this method to reply to enquiries rather than create them
        // For now returning null as this function signature doesn't support reply
        return null;
    }

    /**
     * Retrieves enquiries for a specific project that the officer is assigned to.
     *
     * @param user The Officer.
     * @return A List of Enquiry objects.
     */
    @Override
    public List<Enquiry> viewEnquiries(User user) {
        if (!(user instanceof HdbOfficer)) return Collections.emptyList();
        HdbOfficer officer = (HdbOfficer) user;
        
        // Get all projects officer is assigned to
        List<Project> assignedProjects = new ArrayList<>();
        for (Project project : projectRepo.findAll()) {
            if (isOfficerAssigned(officer, project)) {
                assignedProjects.add(project);
            }
        }
        
        // Get enquiries for all assigned projects
        List<Enquiry> enquiries = new ArrayList<>();
        for (Project project : assignedProjects) {
            enquiries.addAll(enquiryRepo.findByProjectId(project.getProjectId()));
        }
        
        return enquiries;
    }

    /**
     * Retrieves a specific enquiry by ID, ensuring the officer has access to it.
     *
     * @param enquiryId The ID of the enquiry.
     * @param user The Officer requesting the view.
     * @return The Enquiry object or null if not found/not authorized.
     */
    @Override
    public Enquiry viewEnquiryById(String enquiryId, User user) {
        if (!(user instanceof HdbOfficer) || enquiryId == null) return null;
        HdbOfficer officer = (HdbOfficer) user;
        
        Enquiry enquiry = enquiryRepo.findById(enquiryId).orElse(null);
        if (enquiry == null) return null;
        
        // Check if officer is assigned to the project this enquiry is about
        Project project = projectRepo.findById(enquiry.getProjectId()).orElse(null);
        if (project == null) return null;
        
        if (isOfficerAssigned(officer, project)) {
            return enquiry;
        }
        
        return null; // Not authorized
    }

    /**
     * Officers cannot edit enquiries. This method does nothing.
     */
    @Override
    public void editEnquiry(String enquiryId, User user, String newMessage) {
        // Officers cannot edit enquiries, only reply to them
    }

    /**
     * Officers cannot delete enquiries. This method does nothing.
     */
    @Override
    public void deleteEnquiry(String enquiryId, User user) {
        // Officers cannot delete enquiries
    }

    // --- IEligibilityCheck Implementation ---

    /**
     * Checks if an applicant is eligible for a specific flat type based on age and marital status.
     *
     * @param user The User (must be Applicant).
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
            return age >= 21 && (flatType == FlatType.TWOROOM || flatType == FlatType.THREEROOM);
        }

        return false;
    }

    /**
     * Checks if an applicant is eligible to apply for a project.
     *
     * @param user The User (must be Applicant).
     * @param project The Project.
     * @return true if eligible, false otherwise.
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
        return existingApp == null; // Eligible only if no active application
    }

    /**
     * Adds a reply to an enquiry.
     * 
     * @param enquiryId The ID of the enquiry to reply to.
     * @param officer The officer adding the reply.
     * @param replyMessage The reply message.
     * @return true if successful, false otherwise.
     */
    public boolean replyToEnquiry(String enquiryId, HdbOfficer officer, String replyMessage) {
        if (enquiryId == null || officer == null || replyMessage == null || replyMessage.trim().isEmpty()) {
            return false;
        }
        
        Enquiry enquiry = enquiryRepo.findById(enquiryId).orElse(null);
        if (enquiry == null) return false;
        
        // Check if officer is assigned to the project
        Project project = projectRepo.findById(enquiry.getProjectId()).orElse(null);
        if (project == null) return false;
        
        if (!isOfficerAssigned(officer, project)) {
            return false; // Not authorized
        }
        
        // Add the reply
        enquiry.setReply(replyMessage);
        enquiryRepo.update(enquiry);
        
        // Also update in applicant's object if present
        Optional<Applicant> optApplicant = applicantRepo.findById(enquiry.getApplicantId());
        if (optApplicant.isPresent()) {
            Applicant applicant = optApplicant.get();
            if (applicant.getEnquiries() != null) {
                for (Enquiry e : applicant.getEnquiries()) {
                    if (e.getEnquiryId().equals(enquiryId)) {
                        e.setReply(replyMessage);
                        break;
                    }
                }
                applicantRepo.update(applicant);
            }
        }
        
        return true;
    }
    
    /**
     * Processes an application (approve or reject).
     * 
     * @param applicationId The ID of the application to process.
     * @param officer The officer processing the application.
     * @param approve true to approve, false to reject.
     * @return true if successful, false otherwise.
     */
    public boolean processApplication(String applicationId, HdbOfficer officer, boolean approve) {
        if (applicationId == null || officer == null) {
            return false;
        }
        
        Application application = applicationRepo.findById(applicationId).orElse(null);
        if (application == null) return false;
        
        // Check if officer is assigned to the project
        Project project = projectRepo.findById(application.getProjectId()).orElse(null);
        if (project == null) return false;
        
        if (!isOfficerAssigned(officer, project)) {
            return false; // Not authorized
        }
        
        // Check if application is in a state that can be processed
        if (application.getStatus() != ApplStatus.PENDING) {
            return false; // Can only process pending applications
        }
        
        // Update status
        application.setStatus(approve ? ApplStatus.SUCCESS : ApplStatus.REJECT);
        applicationRepo.update(application);
        
        // Update in applicant's object if present
        Optional<Applicant> optApplicant = applicantRepo.findById(application.getApplicantId());
        if (optApplicant.isPresent()) {
            Applicant applicant = optApplicant.get();
            if (applicant.getApplication() != null && 
                applicant.getApplication().getId().equals(applicationId)) {
                applicant.setApplication(application);
                applicantRepo.update(applicant);
            }
        }
        
        return true;
    }
    
    /**
     * Processes a withdrawal request.
     * 
     * @param applicationId The ID of the application with withdrawal request.
     * @param officer The officer processing the request.
     * @param approve true to approve withdrawal, false to reject it.
     * @return true if successful, false otherwise.
     */
    public boolean processWithdrawalRequest(String applicationId, HdbOfficer officer, boolean approve) {
        if (applicationId == null || officer == null) {
            return false;
        }
        
        Application application = applicationRepo.findById(applicationId).orElse(null);
        if (application == null) return false;
        
        // Check if officer is assigned to the project
        Project project = projectRepo.findById(application.getProjectId()).orElse(null);
        if (project == null) return false;
        
        if (!isOfficerAssigned(officer, project)) {
            return false; // Not authorized
        }
        
        // Check if application is in withdrawal pending state
        if (application.getStatus() != ApplStatus.WITHDRAW_PENDING) {
            return false; // Can only process withdrawal requests
        }
        
        // Update status
        application.setStatus(approve ? ApplStatus.WITHDRAW_APPROVED : ApplStatus.PENDING);
        applicationRepo.update(application);
        
        // Update in applicant's object if present
        Optional<Applicant> optApplicant = applicantRepo.findById(application.getApplicantId());
        if (optApplicant.isPresent()) {
            Applicant applicant = optApplicant.get();
            if (applicant.getApplication() != null && 
                applicant.getApplication().getId().equals(applicationId)) {
                applicant.setApplication(application);
                applicantRepo.update(applicant);
            }
        }
        
        return true;
    }
    
    /**
     * Registers an officer for a project.
     * 
     * @param officer The officer to register.
     * @param project The project to register for.
     * @return true if successful, false otherwise.
     */
    public boolean registerForProject(HdbOfficer officer, Project project) {
        if (officer == null || project == null) {
            return false;
        }
        
        // Check if officer is already assigned to this project
        if (isOfficerAssigned(officer, project)) {
            return true; // Already assigned
        }
        
        // Check if there are available slots
        if (project.getOfficers() != null && 
            project.getOfficerSlots() != null && 
            project.getOfficers().size() >= project.getOfficerSlots()) {
            return false; // No slots available
        }
        
        // Add officer to project
        if (project.getOfficers() == null) {
            project.setOfficers(new ArrayList<>());
        }
        project.getOfficers().add(officer);

        // Set officer status
        officer.setStatus(OfficerStatus.PENDING);
        
        // Save changes
        projectRepo.update(project);
        officerRepo.update(officer);
        
        return true;
    }
}