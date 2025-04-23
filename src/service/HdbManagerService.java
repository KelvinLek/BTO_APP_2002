package service;

import entity.*;
import pub_enums.*;
import util.PlaceholderDataUtil;

import java.util.*;

public class HdbManagerService extends ApplicantService implements IHdbManagerService, IManagerProjectView, IManagerEnquiryView, IApprovalService, IWithdrawalApprovalService, IReportService {

    // Basic constructor
    public HdbManagerService() {
        super(); // Rely on the parent's default constructor
    }

    // --- IHdbManagerService Implementation ---

    @Override
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
        Integer units3Room = (Integer) projectDetails.get("units3Room");
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
            flats.add(new Flat(FlatType.TWOROOM, units2Room, units2Room, 150000));
        }
        if (units3Room != null && units3Room > 0) {
            flats.add(new Flat(FlatType.THREEROOM, units3Room, units3Room, 300000));
        }

        // Generate project ID
        String projectId = "P" + String.format("%04d", (int)(Math.random() * 10000));

        // Create new project with minimal constructor and set additional properties
        Project newProject = new Project(name, projectId, neighbourhood, startDate, endDate, manager, null);
        newProject.setFlats(flats);
        newProject.setVisible(isVisible != null ? isVisible : false);
        newProject.setOfficerSlots(slots != null ? slots : 5);

        try {
            PlaceholderDataUtil.saveProjectPlaceholder(newProject);
            return newProject;
        } catch (Exception e) {
            System.err.println("Error creating project: " + e.getMessage());
            throw new RuntimeException("Failed to create project", e);
        }
    }

    @Override
    public boolean updateProject(HdbManager manager, Project project, Map<String, Object> updates) {
        if (manager == null || project == null || updates == null) {
            return false;
        }

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Manager does not have authority to update this project");
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

        try {
            PlaceholderDataUtil.saveProjectPlaceholder(project);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating project: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean assignOfficer(HdbManager manager, String officerId, String projectId) {
        // Find the user (officer)
        User user = PlaceholderDataUtil.findUserByNricPlaceholder(officerId);
        if (user == null || !(user instanceof HdbOfficer)) {
            System.out.println("Invalid officer ID or user is not an officer");
            return false;
        }

        // Find the project
        Project project = PlaceholderDataUtil.findProjectByIdPlaceholder(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return false;
        }

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            System.out.println("Manager does not have authority to assign officers to this project");
            return false;
        }

        // Cast to HdbOfficer
        HdbOfficer officer = (HdbOfficer) user;

        // Check if officer slots are available
        int assignedCount = (project.getOfficers() != null) ? project.getOfficers().size() : 0;
        if (assignedCount >= project.getOfficerSlots()) {
            System.out.println("Project has reached maximum officer slots");
            return false;
        }

        // Initialize officers list if null
        if (project.getOfficers() == null) {
            project.setOfficers(new ArrayList<>());
        }

        // Check if officer is already assigned
        boolean alreadyAssigned = false;
        for (HdbOfficer existingOfficer : project.getOfficers()) {
            if (existingOfficer.getId().equals(officerId)) {
                alreadyAssigned = true;
                break;
            }
        }

        if (alreadyAssigned) {
            System.out.println("Officer is already assigned to this project");
            return false;
        }

        // Add officer to project
        project.getOfficers().add(officer);

        // Add project to officer's assigned projects
        if (officer.getAssignedProjects() == null) {
            officer.setAssignedProjects(new ArrayList<>());
        }
        officer.getAssignedProjects().add(project);

        // Reset officer's status if previously pending
        if (officer.getStatus() != null && officer.getStatus().startsWith("PENDING_REGISTRATION_" + projectId)) {
            officer.setStatus("ASSIGNED");
        }

        try {
            PlaceholderDataUtil.saveProjectPlaceholder(project);
            PlaceholderDataUtil.saveUserPlaceholder(officer);
            return true;
        } catch (Exception e) {
            System.err.println("Error assigning officer: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateApplicationStatus(HdbManager manager, String applicationId, ApplStatus newStatus) {
        // Find the application
        Application application = PlaceholderDataUtil.findApplicationByIdPlaceholder(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }

        // Find the project
        Project project = PlaceholderDataUtil.findProjectByIdPlaceholder(application.getProjectId());
        if (project == null) {
            System.out.println("Project not found");
            return false;
        }

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            System.out.println("Manager does not have authority to update applications for this project");
            return false;
        }

        // Update status
        application.setStatus(newStatus);

        try {
            PlaceholderDataUtil.saveApplicationPlaceholder(application);
            System.out.println("Application status updated to " + newStatus);

            // Notify the applicant of the status change
            // notificationService.notifyApplicant(application.getApplicantId(), "Your application status has been updated to " + newStatus);

            return true;
        } catch (Exception e) {
            System.err.println("Error updating application status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean approveWithdrawal(HdbManager manager, String applicationId) {
        // Find the application
        Application application = PlaceholderDataUtil.findApplicationByIdPlaceholder(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return false;
        }

        // Check if application is in WITHDRAW_PENDING status
        if (application.getStatus() != ApplStatus.WITHDRAW_PENDING) {
            System.out.println("Application is not pending withdrawal");
            return false;
        }

        // Find the project
        Project project = PlaceholderDataUtil.findProjectByIdPlaceholder(application.getProjectId());
        if (project == null) {
            System.out.println("Project not found");
            return false;
        }

        // Verify manager owns the project
        if (project.getManager() == null || !project.getManager().getId().equals(manager.getId())) {
            System.out.println("Manager does not have authority to approve withdrawals for this project");
            return false;
        }

        // Update status to WITHDRAW_APPROVED
        application.setStatus(ApplStatus.WITHDRAW_APPROVED);

        try {
            PlaceholderDataUtil.saveApplicationPlaceholder(application);
            System.out.println("Withdrawal approved for application " + applicationId);

            // If application was already in BOOKED status, increase available flat count
            if (application.getStatus() == ApplStatus.BOOKED && project.getFlats() != null) {
                FlatType flatType;
                try {
                    flatType = FlatType.valueOf(application.getFlatType());
                } catch (IllegalArgumentException e) {
                    flatType = null;
                }

                if (flatType != null) {
                    for (Flat flat : project.getFlats()) {
                        if (flat.getFlatType() == flatType) {
                            flat.setRemaining(flat.getRemaining() + 1); // Return one flat to available pool
                            break;
                        }
                    }
                    PlaceholderDataUtil.saveProjectPlaceholder(project); // Save updated flat counts
                }
            }

            // Notify the applicant of the approval
            // notificationService.notifyApplicant(application.getApplicantId(), "Your withdrawal request has been approved");

            return true;
        } catch (Exception e) {
            System.err.println("Error approving withdrawal: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Project> viewProjectsByManager(HdbManager manager) {
        if (manager == null) {
            return Collections.emptyList();
        }

        List<Project> allProjects = PlaceholderDataUtil.findAllProjectsPlaceholder();
        List<Project> managerProjects = new ArrayList<>();

        for (Project project : allProjects) {
            if (project.getManager() != null && project.getManager().getId().equals(manager.getId())) {
                managerProjects.add(project);
            }
        }

        return managerProjects;
    }

    @Override
    public List<Project> filterAllProjects(Map<String, String> filters, HdbManager manager) {
        List<Project> allProjects = manager != null ?
                viewProjectsByManager(manager) : // Only manager's projects
                PlaceholderDataUtil.findAllProjectsPlaceholder();     // All projects

        if (filters == null || filters.isEmpty()) {
            return allProjects;
        }

        List<Project> filteredProjects = new ArrayList<>();

        for (Project project : allProjects) {
            boolean matches = true;

            for (Map.Entry<String, String> filter : filters.entrySet()) {
                String key = filter.getKey().toLowerCase();
                String value = filter.getValue();

                if (value == null || value.trim().isEmpty()) {
                    continue; // Skip empty filters
                }

                switch (key) {
                    case "name":
                    case "projectname":
                        if (project.getProjName() == null || !project.getProjName().toLowerCase().contains(value.toLowerCase())) {
                            matches = false;
                        }
                        break;
                    case "neighbourhood":
                        if (project.getNeighbourhood() == null || !project.getNeighbourhood().equalsIgnoreCase(value)) {
                            matches = false;
                        }
                        break;
                    case "visible":
                        boolean visibleFilter = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
                        if (project.isVisible() != visibleFilter) {
                            matches = false;
                        }
                        break;
                    // Add more filter types as needed
                }

                if (!matches) {
                    break; // No need to check more filters
                }
            }

            if (matches) {
                filteredProjects.add(project);
            }
        }

        return filteredProjects;
    }

    // --- IEnquiryViewable & IManagerEnquiryView Implementation ---

    @Override
    public List<Enquiry> viewEnquiriesByProject(UUID projectId, HdbManager manager) {
        if (projectId == null) {
            return Collections.emptyList();
        }

        // Find project to verify manager authority
        Project project = PlaceholderDataUtil.findProjectByIdPlaceholder(projectId.toString());
        if (project == null) {
            return Collections.emptyList();
        }

        // Verify manager owns the project
        if (manager != null && project.getManager() != null && !project.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Manager does not have authority to view enquiries for this project");
        }

        return PlaceholderDataUtil.findEnquiriesByProjectPlaceholder(projectId.toString());
    }

    @Override
    public Project viewAnyProjectById(String projectId, HdbManager manager) {
        // Managers can view any project
        return PlaceholderDataUtil.findProjectByIdPlaceholder(projectId);
    }

    @Override
    public Enquiry viewAnyEnquiryById(String enquiryId, HdbManager manager) {
        // Managers can view any enquiry
        // Implementation of IManagerEnquiryView method
        return PlaceholderDataUtil.findEnquiryByIdPlaceholder(enquiryId);
    }

    @Override
    public List<Enquiry> viewAllEnquiries(HdbManager manager) {
        // Implementation of IManagerEnquiryView method
        List<Enquiry> allEnquiries = new ArrayList<>();
        // Collect enquiries from all projects
        for (Project project : PlaceholderDataUtil.findAllProjectsPlaceholder()) {
            allEnquiries.addAll(PlaceholderDataUtil.findEnquiriesByProjectPlaceholder(project.getProjectId()));
        }
        return allEnquiries;
    }

    // Implement missing IManagerProjectView method
    @Override
    public List<Project> viewAllProjects(HdbManager manager) {
        // Return all projects (ignore manager parameter)
        return PlaceholderDataUtil.findAllProjectsPlaceholder();
    }

    // --- IApprovalService Implementation ---
    @Override
    public boolean processApplicationApproval(Application application, HdbManager manager, boolean approve) {
        // Implement the method to avoid abstract class error
        if (application == null || manager == null) {
            return false;
        }

        try {
            // Update application status based on approval decision
            if (approve) {
                application.setStatus(ApplStatus.SUCCESS);
                System.out.println("Application " + application.getId() + " approved successfully");
            } else {
                application.setStatus(ApplStatus.REJECT);
                System.out.println("Application " + application.getId() + " rejected");
            }

            // Save changes
            PlaceholderDataUtil.saveApplicationPlaceholder(application);
            return true;
        } catch (Exception e) {
            System.err.println("Error processing application approval: " + e.getMessage());
            return false;
        }
    }

    // --- IWithdrawalApprovalService Implementation ---
    @Override
    public boolean rejectWithdrawal(Application application, HdbManager manager) {
        if (application == null || manager == null) {
            return false;
        }

        // Check if application is in WITHDRAW_PENDING status
        if (application.getStatus() != ApplStatus.WITHDRAW_PENDING) {
            System.out.println("Application is not pending withdrawal");
            return false;
        }

        try {
            // Revert to previous status (assumed to be PENDING for simplicity)
            application.setStatus(ApplStatus.PENDING);
            PlaceholderDataUtil.saveApplicationPlaceholder(application);
            System.out.println("Withdrawal request rejected for application " + application.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error rejecting withdrawal: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean approveWithdrawal(Application application, HdbManager manager) throws NoSuchElementException, Exception {
        // Delegate to the existing implementation
        return approveWithdrawal(manager, application.getId());
    }

    // Convenience method for controller
    public List<Application> findApplicationsByStatusPlaceholder(ApplStatus status) {
        return PlaceholderDataUtil.findApplicationsByStatusPlaceholder(status);
    }

    /**
     * Finds an officer by their NRIC.
     * @param nric The NRIC of the officer to find
     * @return The HdbOfficer if found, null otherwise
     */
    public HdbOfficer findOfficerByNric(String nric) {
        if (nric == null || nric.trim().isEmpty()) {
            return null;
        }

        User user = PlaceholderDataUtil.findUserByNricPlaceholder(nric);
        if (user != null && user instanceof HdbOfficer) {
            return (HdbOfficer) user;
        }
        return null;
    }

    /**
     * Finds an application by its ID.
     * @param applicationId The ID of the application to find
     * @return The Application if found, null otherwise
     */
    public Application findApplicationById(String applicationId) {
        if (applicationId == null || applicationId.trim().isEmpty()) {
            return null;
        }

        return PlaceholderDataUtil.findApplicationByIdPlaceholder(applicationId);
    }

    /**
     * Finds a user by their NRIC.
     * @param nric The NRIC of the user to find
     * @return The User if found, null otherwise
     */
    public User findUserByNricPlaceholder(String nric) {
        return PlaceholderDataUtil.findUserByNricPlaceholder(nric);
    }

    // --- IReportService Implementation ---
    @Override
    public List<Object> generateBookingReport(Map<String, String> filters) throws Exception {
        // Simple implementation to satisfy the interface
        List<Object> reportItems = new ArrayList<>();
        // In a real implementation, we would query applications or receipts based on filters
        return reportItems;
    }
}