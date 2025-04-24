package service;

import entity.*;
import pub_enums.*;
import repository.*;

import java.util.*;
import java.util.logging.LoggingPermission;

/**
 * Provides services specific to HDB Managers, handling project management,
 * officer assignment, and application approval.
 */
public class HdbManagerService extends UserService implements IProjectView, IReportService {

    private HdbManagerRepo managerRepo;
    private ProjectRepo projectRepo;
    private ApplicationRepo applicationRepo;
    private EnquiryRepo enquiryRepo;
    private HdbOfficerRepo officerRepo;

    public HdbManagerService(HdbManagerRepo managerRepo, ProjectRepo projectRepo, 
                            ApplicationRepo applicationRepo, EnquiryRepo enquiryRepo,
                            HdbOfficerRepo officerRepo) {
        super();
        this.managerRepo = managerRepo;
        this.projectRepo = projectRepo;
        this.applicationRepo = applicationRepo;
        this.enquiryRepo = enquiryRepo;
        this.officerRepo = officerRepo;
    }

    // --- IProjectView Implementation ---

    /**
     * Retrieves the details of any project by its ID, regardless of visibility.
     *
     * @param projectId The ID of the project.
     * @param user The User viewing the project.
     * @return The Project object or null.
     */
    @Override
    public Project viewProjectById(String projectId, User user) {
        if (projectId == null || user == null || !(user instanceof HdbManager)) return null;
        
        // Managers can view any project
        return projectRepo.findById(projectId).orElse(null);
    }

    /**
     * Filters all projects based on given criteria.
     *
     * @param filters Map of filter criteria.
     * @param user The User performing the filter.
     * @return List of matching projects.
     */
    @Override
    public List<Project> filterAllProjects(Map<String, String> filters, User user) {
        if (!(user instanceof HdbManager)) return Collections.emptyList();
        HdbManager manager = (HdbManager) user;
        
        List<Project> allProjects = filters.containsKey("onlymanaged") && 
                                    "true".equalsIgnoreCase(filters.get("onlymanaged")) ?
                                    viewProjectsByUser(user) : // Only manager's projects
                                    projectRepo.findAll();     // All projects
        
        if (filters == null || filters.isEmpty()) {
            return allProjects;
        }

        List<Project> filteredList = new ArrayList<>();
        for (Project project : allProjects) {
            boolean match = true;
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue();
                if (value == null || value.trim().isEmpty() || key.equals("onlymanaged")) continue; // Skip empty filters

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
                    case "name":
                        if (project.getProjName() == null || !project.getProjName().toLowerCase().contains(value.toLowerCase())) {
                            match = false;
                        }
                        break;
                    case "visible":
                        boolean visibleFilter = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
                        if (project.isVisible() != visibleFilter) {
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
     * Retrieves all projects managed by the manager.
     *
     * @param user The User viewing the projects.
     * @return List of projects.
     */
    @Override
    public List<Project> viewProjectsByUser(User user) {
        if (!(user instanceof HdbManager)) return Collections.emptyList();
        HdbManager manager = (HdbManager) user;
        
        return projectRepo.findByManagerId(manager.getId());
    }

    /**
     * Creates a new project.
     * 
     * @param manager The manager creating the project.
     * @param projectDetails Map of project details.
     * @return The created Project object.
     */
    public Project createProject(HdbManager manager, Map<String, Object> projectDetails) {
        if (manager == null || projectDetails == null) {
            throw new IllegalArgumentException("Manager and project details must be provided");
        }

        // Extract project details
        String name = (String) projectDetails.get("projectName");
        String neighbourhood = (String) projectDetails.get("neighbourhood");
        Date startDate = (Date) projectDetails.get("startDate");
        Date endDate = (Date) projectDetails.get("endDate");
        Integer units2Room = (Integer) projectDetails.get("units2Room");
        Double price2Room = (Double) projectDetails.get("price2Room");
        Integer units3Room = (Integer) projectDetails.get("units3Room");
        Double price3Room = (Double) projectDetails.get("price3Room");
        Integer slots = (Integer) projectDetails.get("officerSlots");
        Boolean isVisible = (Boolean) projectDetails.get("isVisible");

        // Validate required fields
        if (name == null || name.trim().isEmpty() || neighbourhood == null || neighbourhood.trim().isEmpty() ||
                startDate == null || endDate == null) {
            throw new IllegalArgumentException("Project name, neighbourhood, and dates are required");
        }

        // Validate dates
        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Initialize flat lists
        List<Flat> flats = new ArrayList<>();
        if (units2Room != null && units2Room > 0) {
            flats.add(new Flat(FlatType.TWOROOM, units2Room, units2Room, price2Room));
        }
        if (units3Room != null && units3Room > 0) {
            flats.add(new Flat(FlatType.THREEROOM, units3Room, units3Room, price3Room));
        }

        // Generate project ID
        String projectId = "P" + String.format("%04d", (int)(Math.random() * 10000));

        // Create new project
        Project newProject = new Project(
                name,
                projectId,
                isVisible != null ? isVisible : false,
                neighbourhood,
                flats,
                startDate,
                endDate,
                manager,
                new ArrayList<>(),
                slots != null ? slots : 5
        );

        projectRepo.add(newProject);
        return newProject;
    }

    /**
     * Updates an existing project.
     * 
     * @param manager The manager updating the project.
     * @param project The project to update.
     * @param updates Map of updates to apply.
     * @return true if successful, false otherwise.
     */
    public boolean updateProject(HdbManager manager, Project project, Map<String, Object> updates) {
        if (manager == null || project == null || updates == null) {
            return false;
        }

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            return false; // Not authorized
        }

        // Apply updates
        if (updates.containsKey("projectName")) {
            String name = (String) updates.get("projectName");
            if (name != null && !name.trim().isEmpty()) {
                project.setProjName(name);
            }
        }

        if (updates.containsKey("neighbourhood")) {
            String neighbourhood = (String) updates.get("neighbourhood");
            if (neighbourhood != null && !neighbourhood.trim().isEmpty()) {
                project.setNeighbourhood(neighbourhood);
            }
        }

        if (updates.containsKey("startDate")) {
            Date startDate = (Date) updates.get("startDate");
            if (startDate != null) {
                project.setAppOpen(startDate);
            }
        }

        if (updates.containsKey("endDate")) {
            Date endDate = (Date) updates.get("endDate");
            if (endDate != null) {
                project.setAppClose(endDate);
            }
        }

        if (updates.containsKey("isVisible") && updates.get("isVisible") instanceof Boolean) {
            project.setVisible((Boolean) updates.get("isVisible"));
        }

        if (updates.containsKey("officerSlots") && updates.get("officerSlots") instanceof Integer) {
            project.setOfficerSlots((Integer) updates.get("officerSlots"));
        }

        projectRepo.update(project);
        return true;
    }

    /**
     * Assigns an officer to a project.
     * 
     * @param manager The manager assigning the officer.
     * @param officerId The ID of the officer to assign.
     * @param projectId The ID of the project to assign to.
     * @return true if successful, false otherwise.
     */
    public boolean assignOfficer(HdbManager manager, String officerId, String projectId) {
        // Find the officer
        Optional<HdbOfficer> optOfficer = officerRepo.findById(officerId);
        if (!optOfficer.isPresent()) {
            return false; // Officer not found
        }
        HdbOfficer officer = optOfficer.get();

        // Find the project
        Optional<Project> optProject = projectRepo.findById(projectId);
        if (!optProject.isPresent()) {
            return false; // Project not found
        }
        Project project = optProject.get();

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            return false; // Not authorized
        }

        // Check if officer slots are available
        int assignedCount = (project.getOfficers() != null) ? project.getOfficers().size() : 0;
        if (assignedCount >= project.getOfficerSlots()) {
            return false; // No slots available
        }

        // Initialize officers list if null
        if (project.getOfficers() == null) {
            project.setOfficers(new ArrayList<>());
        }

        // Check if officer is already assigned
        for (HdbOfficer existingOfficer : project.getOfficers()) {
            if (existingOfficer.getId().equals(officerId)) {
                return true; // Already assigned
            }
        }

        // Add officer to project
        project.getOfficers().add(officer);

        // Update officer status
        officer.setStatus(OfficerStatus.ASSIGNED);

        // Save changes
        projectRepo.update(project);
        officerRepo.update(officer);
        
        return true;
    }

    /**
     * Updates the status of an application.
     * 
     * @param manager The manager updating the status.
     * @param applicationId The ID of the application to update.
     * @param newStatus The new status to set.
     * @return true if successful, false otherwise.
     */
    public boolean updateApplicationStatus(HdbManager manager, String applicationId, ApplStatus newStatus) {
        // Find the application
        Optional<Application> optApp = applicationRepo.findById(applicationId);
        if (!optApp.isPresent()) {
            return false; // Application not found
        }
        Application application = optApp.get();

        // Find the project
        Optional<Project> optProject = projectRepo.findById(application.getProjectId());
        if (!optProject.isPresent()) {
            return false; // Project not found
        }
        Project project = optProject.get();

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            return false; // Not authorized
        }

        // Update status
        application.setStatus(newStatus);
        applicationRepo.update(application);
        
        return true;
    }

    /**
     * Approves a withdrawal request.
     * 
     * @param manager The manager approving the withdrawal.
     * @param applicationId The ID of the application to withdraw.
     * @return true if successful, false otherwise.
     */
    public boolean approveWithdrawal(HdbManager manager, String applicationId) {
        // Find the application
        Optional<Application> optApp = applicationRepo.findById(applicationId);
        if (!optApp.isPresent()) {
            return false; // Application not found
        }
        Application application = optApp.get();

        // Check if application is in WITHDRAW_PENDING status
        if (application.getStatus() != ApplStatus.WITHDRAW_PENDING) {
            return false; // Not pending withdrawal
        }

        // Find the project
        Optional<Project> optProject = projectRepo.findById(application.getProjectId());
        if (!optProject.isPresent()) {
            return false; // Project not found
        }
        Project project = optProject.get();

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            return false; // Not authorized
        }

        // Update status to WITHDRAW_APPROVED
        application.setStatus(ApplStatus.WITHDRAW_APPROVED);
        applicationRepo.update(application);
        
        return true;
    }

    /**
     * Rejects a withdrawal request.
     * 
     * @param manager The manager rejecting the withdrawal.
     * @param applicationId The ID of the application.
     * @return true if successful, false otherwise.
     */
    public boolean rejectWithdrawal(HdbManager manager, String applicationId) {
        // Find the application
        Optional<Application> optApp = applicationRepo.findById(applicationId);
        if (!optApp.isPresent()) {
            return false; // Application not found
        }
        Application application = optApp.get();

        // Check if application is in WITHDRAW_PENDING status
        if (application.getStatus() != ApplStatus.WITHDRAW_PENDING) {
            return false; // Not pending withdrawal
        }

        // Find the project
        Optional<Project> optProject = projectRepo.findById(application.getProjectId());
        if (!optProject.isPresent()) {
            return false; // Project not found
        }
        Project project = optProject.get();

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            return false; // Not authorized
        }

        // Revert to previous status (PENDING)
        application.setStatus(ApplStatus.PENDING);
        applicationRepo.update(application);
        
        return true;
    }

    /**
     * Finds applications by their status.
     * 
     * @param status The status to find.
     * @return List of matching applications.
     */
    public List<Application> findApplicationsByStatus(ApplStatus status) {
        List<Application> allApps = applicationRepo.findAll();
        List<Application> matchingApps = new ArrayList<>();
        
        for (Application app : allApps) {
            if (app.getStatus() == status) {
                matchingApps.add(app);
            }
        }
        
        return matchingApps;
    }

    /**
     * Processes an application for approval or rejection.
     * 
     * @param applicationId The ID of the application to process.
     * @param manager The manager processing the application.
     * @param approve true to approve, false to reject.
     * @return true if successful, false otherwise.
     */
    public boolean processApplication(String applicationId, HdbManager manager, boolean approve) {
        // Find the application
        Optional<Application> optApp = applicationRepo.findById(applicationId);
        if (!optApp.isPresent()) {
            return false; // Application not found
        }
        Application application = optApp.get();
        
        // Find the project
        Optional<Project> optProject = projectRepo.findById(application.getProjectId());
        if (!optProject.isPresent()) {
            return false; // Project not found
        }
        Project project = optProject.get();
        
        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            return false; // Not authorized
        }
        
        // Process application
        if (approve) {
            application.setStatus(ApplStatus.SUCCESS);
        } else {
            application.setStatus(ApplStatus.REJECT);
        }
        
        applicationRepo.update(application);
        return true;
    }

    // --- IReportService Implementation ---

    /**
     * Generates a booking report based on specified filters.
     *
     * @param filters Map of filter criteria.
     * @return List of report data.
     */
    @Override
    public List<Object> generateBookingReport(Map<String, String> filters) {
        List<Object> reportData = new ArrayList<>();
        
        // Collect all applications
        List<Application> applications = applicationRepo.findAll();
        
        // Apply filters
        if (filters != null && !filters.isEmpty()) {
            // Filter by status if specified
            if (filters.containsKey("status")) {
                try {
                    ApplStatus status = ApplStatus.valueOf(filters.get("status").toUpperCase());
                    applications = applications.stream()
                            .filter(app -> app.getStatus() == status)
                            .collect(java.util.stream.Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore filter
                }
            }
            
            // Filter by project if specified
            if (filters.containsKey("projectId")) {
                String projectId = filters.get("projectId");
                applications = applications.stream()
                        .filter(app -> app.getProjectId().equals(projectId))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Add more filters as needed
        }
        
        // Transform applications to report data
        for (Application app : applications) {
            Map<String, Object> reportItem = new HashMap<>();
            reportItem.put("applicationId", app.getId());
            reportItem.put("applicantId", app.getApplicantId());
            reportItem.put("projectId", app.getProjectId());
            reportItem.put("status", app.getStatus().name());
            reportItem.put("flatType", app.getFlatType());
            
            // Add project details if available
            Project project = projectRepo.findById(app.getProjectId()).orElse(null);
            if (project != null) {
                reportItem.put("projectName", project.getProjName());
                reportItem.put("neighbourhood", project.getNeighbourhood());
            }
            
            reportData.add(reportItem);
        }
        
        return reportData;
    }

    public void deleteProject(HdbManager manager, Project project) {
        if (manager == null || project == null) {
            throw new IllegalArgumentException("Manager and project details must be provided");
        }
        projectRepo.delete(project);
    }
}