package service;

import entity.*;
import pub_enums.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
// Import necessary repository interfaces if interacting with data storage
// import repository.*;

/**
 * Provides services specific to HDB Managers, including project management (CRUD),
 * visibility control, officer/applicant approvals, reporting, and enquiry handling.
 */
public class HdbManagerService extends UserService implements IEnquiryViewable, IManagerService, IApprovalService, IManagerEnquiryView, IManagerProjectView, IReplyable, IWithdrawalApprovalService, IReportService {

    // Assume repository injection
    // private final IHdbManagerRepo managerRepo; // Specific repo for manager data/status
    // private final IProjectRepo projectRepo;
    // private final IApplicationRepo applicationRepo;
    // private final IHdbOfficerRepo officerRepo;
    // private final IApplicantRepo applicantRepo; // Needed for reports
    // private final IEnquiryRepo enquiryRepo;
    // private final IReceiptRepo receiptRepo; // Needed for reports

    // Constructor example (adjust based on actual dependencies)
    // public HdbManagerService(IUserRepo userRepo, IHdbManagerRepo managerRepo, IProjectRepo projectRepo,
    //                          IApplicationRepo applicationRepo, IHdbOfficerRepo officerRepo, IApplicantRepo applicantRepo,
    //                          IEnquiryRepo enquiryRepo, IReceiptRepo receiptRepo) {
    //     super(userRepo); // Call parent constructor
    //     this.managerRepo = managerRepo;
    //     this.projectRepo = projectRepo;
    //     this.applicationRepo = applicationRepo;
    //     this.officerRepo = officerRepo;
    //     this.applicantRepo = applicantRepo;
    //     this.enquiryRepo = enquiryRepo;
    //     this.receiptRepo = receiptRepo;
    // }

    // --- IManagerService Implementation ---

    /**
     * Creates a new BTO project. Checks manager eligibility (no overlapping assignments).
     *
     * @param manager        The HdbManager creating the project.
     * @param projectDetails Map containing project details. Keys like "projectName", "neighbourhood", "units2Room", "units3Room", "startDate", "endDate", "officerSlots", "isVisible".
     * @return The newly created Project object.
     * @throws IllegalArgumentException If details are invalid or manager has overlap.
     * @throws Exception                For persistence errors.
     */
    @Override
    public Project createProject(HdbManager manager, Map<String, Object> projectDetails) throws IllegalArgumentException, Exception {
        if (manager == null || manager.getId() == null || projectDetails == null) {
            throw new IllegalArgumentException("Manager and project details are required.");
        }

        // 1. Extract and validate details
        String name = (String) projectDetails.get("projectName");
        String neighbourhood = (String) projectDetails.get("neighbourhood");
        Integer units2R = (Integer) projectDetails.getOrDefault("units2Room", 0);
        Integer units3R = (Integer) projectDetails.getOrDefault("units3Room", 0);
        String startDateStr = (String) projectDetails.get("startDate");
        String endDateStr = (String) projectDetails.get("endDate");
        Integer slots = (Integer) projectDetails.getOrDefault("officerSlots", 0);
        Boolean isVisible = (Boolean) projectDetails.getOrDefault("isVisible", false);

        if (name == null || name.trim().isEmpty() || neighbourhood == null || neighbourhood.trim().isEmpty() ||
                startDateStr == null || endDateStr == null || units2R < 0 || units3R < 0 || slots < 0 || slots > 10) {
            throw new IllegalArgumentException("Invalid project details provided (Name, Neighbourhood, Dates required; Units/Slots non-negative, Slots <= 10).");
        }

        Date startDate, endDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Or appropriate format
            sdf.setLenient(false);
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
            if(endDate.before(startDate)) {
                throw new IllegalArgumentException("End date cannot be before start date.");
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format (use yyyy-MM-dd).", e);
        }


        // 2. Check manager eligibility (no overlap with their existing projects)
        List<Project> existingProjects = findProjectsByManagerPlaceholder(manager.getId()); // Placeholder
        if(existingProjects != null){
            for(Project existing : existingProjects){
                if (existing.getAppOpen() != null && existing.getAppClose() != null) {
                    // Check for overlap: (StartA <= EndB) and (EndA >= StartB)
                    if (!startDate.after(existing.getAppClose()) && !endDate.before(existing.getAppOpen())) {
                        throw new IllegalArgumentException("Manager already handles project '" + existing.getProjName() + "' during this period.");
                    }
                }
            }
        }


        // 3. Create Project entity
        Project newProject = new Project(name, neighbourhood, startDate, endDate, manager, null /* officers list */);
        newProject.setProjectId(UUID.randomUUID().toString()); // Generate ID
        newProject.setVisible(isVisible);
        newProject.setOfficerSlots(slots);

        List<Flat> flats = new ArrayList<>();
        if (units2R > 0) flats.add(new Flat(FlatType.TWOROOM, units2R, units2R, 0)); // Price TBD/not specified
        if (units3R > 0) flats.add(new Flat(FlatType.THREEROOM, units3R, units3R, 0)); // Price TBD/not specified
        newProject.setFlats(flats);

        // 4. Save the new project
        try {
            // projectRepo.save(newProject); // Example repo call
            saveProjectPlaceholder(newProject); // Placeholder
            // Associate project with manager if needed
            manager.addCreatedProject(newProject);
            // saveUserPlaceholder(manager); // If needed
            System.out.println("Project '" + name + "' created successfully (ID: " + newProject.getProjectId() + ")");
            return newProject;
        } catch (Exception e) {
            System.err.println("Error saving new project: " + e.getMessage());
            throw new Exception("Failed to create project.", e);
        }
    }

    /**
     * Edits an existing project. Only the manager in charge can edit.
     *
     * @param projectId      The ID of the project to edit.
     * @param updatedDetails Map containing fields to update.
     * @param editor         The HdbManager attempting the edit.
     * @return true if update successful.
     * @throws NoSuchElementException   If project not found.
     * @throws SecurityException        If editor is not the manager in charge.
     * @throws IllegalArgumentException If details are invalid.
     * @throws Exception                For persistence errors.
     */
    @Override
    public boolean editProject(String projectId, Map<String, Object> updatedDetails, HdbManager editor) throws NoSuchElementException, SecurityException, IllegalArgumentException, Exception {
        if (projectId == null || updatedDetails == null || editor == null || editor.getId() == null) {
            throw new IllegalArgumentException("Project ID, update details, and editor ID are required.");
        }

        // 1. Find the project
        Project project = findProjectByIdPlaceholder(projectId); // Placeholder
        if (project == null) {
            throw new NoSuchElementException("Project with ID " + projectId + " not found.");
        }

        // 2. Check permission
        if (project.getManager() == null || !project.getManager().getId().equals(editor.getId())) {
            throw new SecurityException("User " + editor.getId() + " is not the manager in charge of project " + projectId);
        }


        // 3. Apply updates (with validation)
        boolean changed = false;
        if (updatedDetails.containsKey("projectName")) {
            String val = (String) updatedDetails.get("projectName");
            if (val != null && !val.trim().isEmpty()) { project.setProjName(val); changed = true; }
            else throw new IllegalArgumentException("Project Name cannot be empty.");
        }
        if (updatedDetails.containsKey("neighbourhood")) {
            String val = (String) updatedDetails.get("neighbourhood");
            if (val != null && !val.trim().isEmpty()) { project.setNeighbourhood(val); changed = true; }
            else throw new IllegalArgumentException("Neighbourhood cannot be empty.");
        }
        // Update flat counts (handle carefully - ensure flat types exist)
        if (updatedDetails.containsKey("units2Room")) {
            Integer val = (Integer) updatedDetails.get("units2Room");
            if(val == null || val < 0) throw new IllegalArgumentException("2-Room units cannot be negative.");
            updateFlatCount(project, FlatType.TWOROOM, val); changed = true;
        }
        if (updatedDetails.containsKey("units3Room")) {
            Integer val = (Integer) updatedDetails.get("units3Room");
            if(val == null || val < 0) throw new IllegalArgumentException("3-Room units cannot be negative.");
            updateFlatCount(project, FlatType.THREEROOM, val); changed = true;
        }
        // Update dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); sdf.setLenient(false);
        Date newStartDate = project.getAppOpen(); Date newEndDate = project.getAppClose();
        try {
            if (updatedDetails.containsKey("startDate")) {
                newStartDate = sdf.parse((String) updatedDetails.get("startDate")); changed = true;
            }
            if (updatedDetails.containsKey("endDate")) {
                newEndDate = sdf.parse((String) updatedDetails.get("endDate")); changed = true;
            }
            if (newEndDate.before(newStartDate)) throw new IllegalArgumentException("End date cannot be before start date.");
            project.setAppOpen(newStartDate); project.setAppClose(newEndDate);
        } catch (ParseException e){ throw new IllegalArgumentException("Invalid date format."); }
        catch (ClassCastException e){ throw new IllegalArgumentException("Invalid type for date string."); }
        // Update slots
        if (updatedDetails.containsKey("officerSlots")) {
            Integer val = (Integer) updatedDetails.get("officerSlots");
            if(val == null || val < 0 || val > 10) throw new IllegalArgumentException("Officer slots must be between 0 and 10.");
            project.setOfficerSlots(val); changed = true;
        }
        // Update visibility is handled by toggleProjectVisibility


        // 4. Save if changes were made
        if (changed) {
            try {
                saveProjectPlaceholder(project); // Placeholder
                System.out.println("Project " + projectId + " updated successfully.");
                return true;
            } catch (Exception e) {
                System.err.println("Error saving updated project: " + e.getMessage());
                // Rollback changes in object? Complex.
                throw new Exception("Failed to save project updates.", e);
            }
        } else {
            System.out.println("No valid changes detected for project " + projectId + ". Update skipped.");
            return false; // Indicate no update occurred
        }
    }

    // Helper to update flat counts during editProject
    private void updateFlatCount(Project project, FlatType type, int newTotal) {
        boolean found = false;
        if(project.getFlats() == null) project.setFlats(new ArrayList<>());

        for(Flat flat : project.getFlats()){
            if(flat.getFlatType() == type){
                // Adjust remaining count if total decreases below current remaining
                if(newTotal < flat.getRemaining()){
                    System.out.println("Warning: New total units ("+newTotal+") for "+type+" is less than remaining ("+flat.getRemaining()+"). Setting remaining equal to total.");
                    flat.setRemaining(newTotal);
                }
                flat.setTotal(newTotal);
                found = true;
                break;
            }
        }
        if(!found && newTotal > 0){ // Add new flat type if it wasn't there before
            project.getFlats().add(new Flat(type, newTotal, newTotal, 0));
        } else if (!found && newTotal <= 0) {
            // Do nothing if trying to set non-existent flat type to 0
        } else if (found && newTotal <= 0){
            // Remove flat type if total is set to 0? Design choice.
            project.getFlats().removeIf(f -> f.getFlatType() == type);
            System.out.println("Removed "+type+" from project as total units set to 0.");
        }
    }


    /**
     * Deletes a project. Only the manager in charge can delete.
     * Consider implications (cannot delete if applications exist?). Brief doesn't specify.
     *
     * @param projectId The ID of the project to delete.
     * @param deleter   The HdbManager attempting deletion.
     * @return true if deletion successful.
     * @throws NoSuchElementException If project not found.
     * @throws SecurityException      If deleter is not the manager.
     * @throws IllegalStateException  If deletion is disallowed (e.g., active applications).
     * @throws Exception              For persistence errors.
     */
    @Override
    public boolean deleteProject(String projectId, HdbManager deleter) throws NoSuchElementException, SecurityException, IllegalStateException, Exception {
        if (projectId == null || deleter == null || deleter.getId() == null) {
            throw new IllegalArgumentException("Project ID and deleter ID are required.");
        }

        // 1. Find project
        Project project = findProjectByIdPlaceholder(projectId); // Placeholder
        if (project == null) {
            throw new NoSuchElementException("Project with ID " + projectId + " not found.");
        }

        // 2. Check permission
        if (project.getManager() == null || !project.getManager().getId().equals(deleter.getId())) {
            throw new SecurityException("User " + deleter.getId() + " is not the manager in charge of project " + projectId);
        }

        // 3. Check if deletable (e.g., no active/booked applications?) - Brief is unclear.
        // List<Application> apps = applicationRepo.findByProjectId(projectId); // Example repo call
        List<Application> apps = findApplicationsByProjectPlaceholder(projectId); // Placeholder
        boolean hasNonFinalApps = false;
        if(apps != null){
            for(Application app : apps){
                // Define "non-final" statuses more broadly if needed
                if(app.getStatus() == ApplStatus.PENDING || app.getStatus() == ApplStatus.SUCCESS ||
                        app.getStatus() == ApplStatus.BOOKED || app.getStatus() == ApplStatus.WITHDRAW_PENDING ) {
                    hasNonFinalApps = true;
                    break;
                }
            }
        }
        if(hasNonFinalApps){
            throw new IllegalStateException("Project " + projectId + " cannot be deleted as it has associated applications.");
        }


        // 4. Delete the project
        try {
            // projectRepo.deleteById(projectId); // Example repo call
            deleteProjectPlaceholder(projectId); // Placeholder
            System.out.println("Project " + projectId + " deleted successfully.");
            // Remove from manager's created list?
            deleter.removeCreatedProject(project); // Requires method in HdbManager entity
            // saveUserPlaceholder(deleter); // If needed
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting project: " + e.getMessage());
            throw new Exception("Failed to delete project.", e);
        }
    }


    /**
     * Toggles the visibility of a project. Only manager in charge can toggle.
     *
     * @param projectId The project ID.
     * @param isVisible The desired visibility state.
     * @param toggler   The HdbManager performing the action.
     * @return true if successful.
     * @throws NoSuchElementException If project not found.
     * @throws SecurityException      If toggler is not the manager.
     * @throws Exception              For persistence errors.
     */
    @Override
    public boolean toggleProjectVisibility(String projectId, boolean isVisible, HdbManager toggler) throws NoSuchElementException, SecurityException, Exception {
        if (projectId == null || toggler == null || toggler.getId() == null) {
            throw new IllegalArgumentException("Project ID and toggler ID are required.");
        }

        // 1. Find project
        Project project = findProjectByIdPlaceholder(projectId); // Placeholder
        if (project == null) {
            throw new NoSuchElementException("Project with ID " + projectId + " not found.");
        }

        // 2. Check permission
        if (project.getManager() == null || !project.getManager().getId().equals(toggler.getId())) {
            throw new SecurityException("User " + toggler.getId() + " is not the manager in charge of project " + projectId);
        }

        // 3. Update visibility
        if (project.isVisible() == isVisible) {
            System.out.println("Project " + projectId + " visibility is already set to " + isVisible + ". No change made.");
            return false; // No change needed
        }
        project.setVisible(isVisible);


        // 4. Save the change
        try {
            saveProjectPlaceholder(project); // Placeholder
            System.out.println("Project " + projectId + " visibility set to " + isVisible);
            return true;
        } catch (Exception e) {
            System.err.println("Error toggling project visibility: " + e.getMessage());
            // project.setVisible(!isVisible); // Rollback
            throw new Exception("Failed to update project visibility.", e);
        }
    }

    /**
     * Processes an HDB Officer's registration request. Approves or rejects.
     * Checks eligibility and slots if approving.
     *
     * @param officer   The HdbOfficer whose registration is processed.
     * @param project   The Project registered for.
     * @param manager   The HdbManager performing the action.
     * @param approve   True to approve, false to reject.
     * @return true if status updated successfully.
     * @throws NoSuchElementException   If officer/project not found.
     * @throws SecurityException        If manager not in charge.
     * @throws IllegalStateException    If approval conditions not met.
     * @throws Exception                For persistence errors.
     */
    @Override
    public boolean processOfficerRegistration(HdbOfficer officer, Project project, HdbManager manager, boolean approve) throws NoSuchElementException, SecurityException, IllegalStateException, Exception {
        if (officer == null || project == null || manager == null || officer.getId() == null || project.getProjectId() == null || manager.getId() == null) {
            throw new IllegalArgumentException("Officer, Project, and Manager details are required.");
        }

        // 1. Verify project exists and manager is in charge
        Project projToAssign = findProjectByIdPlaceholder(project.getProjectId()); // Placeholder
        if (projToAssign == null) {
            throw new NoSuchElementException("Project " + project.getProjectId() + " not found.");
        }
        if (projToAssign.getManager() == null || !projToAssign.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Manager " + manager.getId() + " is not in charge of project " + project.getProjectId());
        }

        // 2. Verify officer exists
        HdbOfficer officerToProcess = (HdbOfficer) findUserByNricPlaceholder(officer.getId()); // Placeholder
        if (officerToProcess == null || !(officerToProcess instanceof HdbOfficer)) {
            throw new NoSuchElementException("HDB Officer " + officer.getId() + " not found.");
        }


        // 3. Check current status (ensure it's pending or handle other states appropriately)
        // String currentRegStatus = officerRepo.findRegistrationStatus(officer.getId(), project.getId()); // Example repo call
        String currentRegStatus = viewOfficerRegistrationStatusPlaceholder(officer.getId(), project.getProjectId()); // Placeholder
        // if (!"PENDING".equals(currentRegStatus)) { // Assuming PENDING status exists
        //      throw new IllegalStateException("Officer registration for project " + project.getId() + " is not in PENDING state.");
        // }


        if (approve) {
            // 4a. Check eligibility (overlap with other *approved* assignments)
            List<Project> assignedProjects = officerToProcess.getAssignedProjects();
            if(assignedProjects != null && projToAssign.getAppOpen() != null && projToAssign.getAppClose() != null){
                for (Project assigned : assignedProjects) {
                    if (assigned.getAppOpen() != null && assigned.getAppClose() != null) {
                        if (!projToAssign.getAppOpen().after(assigned.getAppClose()) && !projToAssign.getAppClose().before(assigned.getAppOpen())) {
                            // Reject if overlap found
                            return processOfficerRegistration(officer, project, manager, false); // Call self to reject
                            // OR throw new IllegalStateException("Approval failed: Officer already assigned to overlapping project '" + assigned.getProjName() + "'.");
                        }
                    }
                }
            }

            // 4b. Check slots
            if (projToAssign.getOfficerSlots() <= 0) {
                // Reject if no slots
                return processOfficerRegistration(officer, project, manager, false); // Call self to reject
                // OR throw new IllegalStateException("Approval failed: No available officer slots for project " + project.getId());
            }


            // 4c. Approve: Update officer status, add to assigned list, decrement project slots
            officerToProcess.setStatus("Approved"); // Or more specific status like APPROVED_FOR_PROJECT_ID
            officerToProcess.addAssignedProject(projToAssign);
            projToAssign.setOfficerSlots(projToAssign.getOfficerSlots() - 1);

        } else {
            // 4d. Reject: Update officer status
            officerToProcess.setStatus("Rejected - Project " + project.getProjectId()); // Example specific rejection status
            // Remove from assigned list if it was added tentatively? Depends on registration flow.
        }

        // 5. Save changes
        try {
            saveUserPlaceholder(officerToProcess); // Save officer status/assignments
            if(approve) saveProjectPlaceholder(projToAssign); // Save updated slot count if approved
            System.out.println("Officer " + officer.getId() + " registration for project " + project.getProjectId() + " processed: " + (approve ? "Approved" : "Rejected"));
            return true;
        } catch (Exception e) {
            System.err.println("Error processing officer registration: " + e.getMessage());
            // Rollback changes? Complex.
            throw new Exception("Failed to process officer registration.", e);
        }
    }

    // --- IApprovalService Implementation ---

    /**
     * Processes application approval/rejection. Checks flat availability if approving.
     * Updates application status and project flat counts.
     *
     * @param application The Application to process.
     * @param manager     The HdbManager performing the action.
     * @param approve     True to approve, false to reject.
     * @return true if status updated successfully.
     * @throws NoSuchElementException If application/project not found.
     * @throws IllegalStateException  If approving without units or application not in correct state.
     * @throws Exception              For persistence errors.
     */
    @Override
    public boolean processApplicationApproval(Application application, HdbManager manager, boolean approve) throws NoSuchElementException, IllegalStateException, Exception {
        if (application == null || application.getId() == null || manager == null) {
            throw new IllegalArgumentException("Application and Manager details required.");
        }

        // 1. Find application
        Application appToProcess = findApplicationByIdPlaceholder(application.getId()); // Placeholder
        if (appToProcess == null) {
            throw new NoSuchElementException("Application " + application.getId() + " not found.");
        }

        // 2. Check current status (should be PENDING)
        if (appToProcess.getStatus() != ApplStatus.PENDING) {
            throw new IllegalStateException("Application " + application.getId() + " is not in PENDING status (Current: " + appToProcess.getStatus() + ").");
        }

        // 3. Find project
        Project project = findProjectByIdPlaceholder(appToProcess.getProjectId()); // Placeholder
        if (project == null) {
            throw new NoSuchElementException("Project " + appToProcess.getProjectId() + " associated with the application not found.");
        }


        if (approve) {
            // 4a. Check flat availability
            FlatType requestedType;
            try {
                requestedType = FlatType.valueOf(appToProcess.getFlatType());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid flat type stored in application: " + appToProcess.getFlatType());
            }

            boolean available = false;
            Flat targetFlat = null;
            if(project.getFlats() != null){
                for(Flat flat : project.getFlats()){
                    if(flat.getFlatType() == requestedType){
                        targetFlat = flat;
                        if(flat.getRemaining() > 0){
                            available = true;
                        }
                        break;
                    }
                }
            }

            if (!available || targetFlat == null) {
                // If not available, reject automatically
                return processApplicationApproval(application, manager, false); // Call self to reject
                // OR throw new IllegalStateException("Approval failed: No units available for " + requestedType + " in project " + project.getProjectId());
            }

            // 4b. Approve: Update status, decrement count
            appToProcess.setStatus(ApplStatus.SUCCESS);
            targetFlat.setRemaining(targetFlat.getRemaining() - 1);

        } else {
            // 4c. Reject: Update status
            appToProcess.setStatus(ApplStatus.REJECT);
        }


        // 5. Save changes
        try {
            saveApplicationPlaceholder(appToProcess); // Save application status
            if(approve) saveProjectPlaceholder(project); // Save updated flat count if approved
            System.out.println("Application " + application.getId() + " processed: " + appToProcess.getStatus());
            return true;
        } catch (Exception e) {
            System.err.println("Error processing application approval: " + e.getMessage());
            // Rollback?
            throw new Exception("Failed to process application approval.", e);
        }
    }


    // --- IManagerEnquiryView Implementation ---

    /**
     * Retrieves all enquiries (Manager view).
     * @param manager Contextual manager object.
     * @return List of all enquiries.
     */
    @Override
    public List<Enquiry> viewAllEnquiries(HdbManager manager) {
        // Permission check might be implicit by calling this manager-specific method
        return findAllEnquiriesPlaceholder(); // Placeholder
    }

    /**
     * Retrieves enquiries by project ID (Manager view).
     * @param projectId Project ID.
     * @param manager   Contextual manager object.
     * @return List of enquiries for the project.
     */
    @Override
    public List<Enquiry> viewEnquiriesByProject(UUID projectId, HdbManager manager) {
        // Permission check might be implicit
        if(projectId == null) return Collections.emptyList();
        return findEnquiriesByProjectPlaceholder(projectId.toString()); // Placeholder
    }

    /**
     * Retrieves a single enquiry by ID (Manager view).
     * @param enquiryId Enquiry ID.
     * @param manager   Contextual manager object.
     * @return The Enquiry or null.
     */
    @Override
    public Enquiry viewAnyEnquiryById(String enquiryId, HdbManager manager) {
        // Permission check might be implicit
        if(enquiryId == null) return null;
        return findEnquiryByIdPlaceholder(enquiryId); // Placeholder
    }


    // --- IManagerProjectView Implementation ---

    /**
     * Retrieves all projects (Manager view).
     * @param manager Contextual manager object.
     * @return List of all projects.
     */
    @Override
    public List<Project> viewAllProjects(HdbManager manager) {
        // Permission check might be implicit
        return findAllProjectsPlaceholder(); // Placeholder
    }

    /**
     * Retrieves projects created by a specific manager.
     * @param manager The HdbManager.
     * @return List of their created projects.
     */
    @Override
    public List<Project> viewProjectsByManager(HdbManager manager) {
        if(manager == null || manager.getId() == null) return Collections.emptyList();
        // This could use the manager's internal list or a repo call
        // return manager.getCreatedProjects(); // If entity holds the list
        return findProjectsByManagerPlaceholder(manager.getId()); // Placeholder using repo call simulation
    }

    /**
     * Retrieves any project by ID (Manager view).
     * @param projectId Project ID.
     * @param manager   Contextual manager object.
     * @return The Project or null.
     */
    @Override
    public Project viewAnyProjectById(String projectId, HdbManager manager) {
        // Permission check might be implicit
        if(projectId == null) return null;
        return findProjectByIdPlaceholder(projectId); // Placeholder
    }

    /**
     * Filters all projects based on criteria (Manager view).
     * @param filters Map of filter criteria.
     * @param manager Contextual manager object.
     * @return List of matching projects.
     */
    @Override
    public List<Project> filterAllProjects(Map<String, String> filters, HdbManager manager) {
        List<Project> allProjects = viewAllProjects(manager); // Get all projects first
        if (filters == null || filters.isEmpty()) {
            return allProjects; // No filters
        }

        // Apply filtering logic (similar to filterApplicantProjects but on all projects)
        List<Project> filteredList = new ArrayList<>();
        for (Project project : allProjects) {
            boolean match = true;
            // Apply filters from map... (logic copied & adapted from ApplicantService)
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue();
                if (value == null || value.trim().isEmpty()) continue;

                switch (key) {
                    case "neighbourhood":
                        if (project.getNeighbourhood() == null || !project.getNeighbourhood().equalsIgnoreCase(value)) match = false;
                        break;
                    case "flattype":
                        try {
                            FlatType requestedType = FlatType.valueOf(value.toUpperCase());
                            boolean offersType = false;
                            if(project.getFlats() != null){
                                for(Flat flat : project.getFlats()){ if(flat.getFlatType() == requestedType){ offersType = true; break;}}
                            }
                            if(!offersType) match = false;
                        } catch (IllegalArgumentException e) { match = false; }
                        break;
                    case "projectname":
                        if (project.getProjName() == null || !project.getProjName().toLowerCase().contains(value.toLowerCase())) match = false;
                        break;
                    case "managerid": // Filter by specific manager ID
                        if (project.getManager() == null || !project.getManager().getId().equals(value)) match = false;
                        break;
                    default: break; // Ignore unknown keys
                }
                if (!match) break;
            }
            if (match) {
                filteredList.add(project);
            }
        }
        return filteredList;
    }

    // --- IReplyable Implementation --- (Manager context)
    // Managers can reply to any enquiry.
    @Override
    public boolean replyToEnquiry(Enquiry enquiry, String replyText, User replyingUser) throws NoSuchElementException, SecurityException, Exception {
        // Re-use the implementation from HdbOfficerService which already handles Manager role
        // This requires HdbOfficerService to be available/injected, or duplicate the logic here.
        // For simplicity, assuming the logic is duplicated or accessed via a shared helper/parent.
        // Let's duplicate the core logic:
        if (enquiry == null || enquiry.getEnquiryId() == null || replyText == null || replyText.trim().isEmpty() || replyingUser == null || replyingUser.getId() == null) {
            throw new IllegalArgumentException("Enquiry, reply text, and replying user details are required.");
        }
        if (replyingUser.getRole() != Role.HDBMANAGER && replyingUser.getRole() != Role.HDBOFFICER /* Check if officer logic needed here */) {
            throw new SecurityException("User must be an HDB Manager (or authorized Officer) to reply.");
        }

        Enquiry existingEnquiry = findEnquiryByIdPlaceholder(enquiry.getEnquiryId());
        if (existingEnquiry == null) throw new NoSuchElementException("Enquiry not found.");

        existingEnquiry.setReply(replyText + " [Replied by Manager: " + replyingUser.getId() + "]");

        try {
            saveEnquiryPlaceholder(existingEnquiry);
            System.out.println("Reply added to enquiry " + existingEnquiry.getEnquiryId() + " by Manager " + replyingUser.getId());
            return true;
        } catch (Exception e) { throw new Exception("Failed to save manager reply.", e); }
    }

    // --- IWithdrawalApprovalService Implementation ---

    /**
     * Approves an applicant's withdrawal request. Updates application status.
     * Potentially restores flat unit count if applicable.
     *
     * @param application The Application (should be status WITHDRAW_PENDING).
     * @param manager     The HdbManager approving.
     * @return true if successful.
     * @throws NoSuchElementException If application not found or not in correct state.
     * @throws Exception              For persistence errors.
     */
    @Override
    public boolean approveWithdrawal(Application application, HdbManager manager) throws NoSuchElementException, Exception {
        if (application == null || application.getId() == null || manager == null) {
            throw new IllegalArgumentException("Application and Manager details required.");
        }

        // 1. Find application
        Application appToProcess = findApplicationByIdPlaceholder(application.getId()); // Placeholder
        if (appToProcess == null) throw new NoSuchElementException("Application not found.");

        // 2. Check status is WITHDRAW_PENDING
        if (appToProcess.getStatus() != ApplStatus.WITHDRAW_PENDING) {
            throw new IllegalStateException("Application is not pending withdrawal (Current: " + appToProcess.getStatus() + ").");
        }

        // 3. Update status
        ApplStatus previousStatusBeforeWithdrawRequest = ApplStatus.PENDING; // Need to know what state it was in before WITHDRAW_PENDING
        // This requires better state tracking or application history, which is missing.
        // For now, assume we just mark it withdrawn.
        appToProcess.setStatus(ApplStatus.WITHDRAW_APPROVED);

        // 4. Restore flat unit if it was booked/successful (Complex - requires knowing previous state)
        // boolean wasAllocated = (previousStatusBeforeWithdrawRequest == ApplStatus.SUCCESS || previousStatusBeforeWithdrawRequest == ApplStatus.BOOKED);
        // if(wasAllocated) {
        //      Project project = findProjectByIdPlaceholder(appToProcess.getProjectId());
        //      FlatType type = FlatType.valueOf(appToProcess.getFlatType());
        //      if(project != null && project.getFlats() != null) {
        //          for(Flat flat : project.getFlats()){
        //               if(flat.getFlatType() == type){
        //                   flat.setRemaining(flat.getRemaining() + 1); // Increment
        //                   saveProjectPlaceholder(project); // Save updated project
        //                   break;
        //               }
        //           }
        //      }
        // }


        // 5. Save application status
        try {
            saveApplicationPlaceholder(appToProcess);
            System.out.println("Withdrawal approved for application " + application.getId());
            return true;
        } catch (Exception e) { throw new Exception("Failed to approve withdrawal.", e); }
    }

    /**
     * Rejects an applicant's withdrawal request. Updates application status.
     * Status may revert to previous state or a specific "Withdraw-Rejected" state.
     *
     * @param application The Application (should be status WITHDRAW_PENDING).
     * @param manager     The HdbManager rejecting.
     * @return true if successful.
     * @throws NoSuchElementException If application not found or not in correct state.
     * @throws Exception              For persistence errors.
     */
    @Override
    public boolean rejectWithdrawal(Application application, HdbManager manager) throws NoSuchElementException, Exception {
        if (application == null || application.getId() == null || manager == null) {
            throw new IllegalArgumentException("Application and Manager details required.");
        }

        Application appToProcess = findApplicationByIdPlaceholder(application.getId());
        if (appToProcess == null) throw new NoSuchElementException("Application not found.");

        if (appToProcess.getStatus() != ApplStatus.WITHDRAW_PENDING) {
            throw new IllegalStateException("Application is not pending withdrawal (Current: " + appToProcess.getStatus() + ").");
        }

        // Determine state to revert to (Needs history/better state machine)
        // For now, just set a generic rejection status or revert to PENDING if simple.
        ApplStatus revertToStatus = ApplStatus.PENDING; // Simplistic assumption
        // OR: appToProcess.setStatus(ApplStatus.WITHDRAW_REJECTED); // If such a status exists
        appToProcess.setStatus(revertToStatus);


        try {
            saveApplicationPlaceholder(appToProcess);
            System.out.println("Withdrawal rejected for application " + application.getId() + ". Status reverted to " + revertToStatus);
            return true;
        } catch (Exception e) { throw new Exception("Failed to reject withdrawal.", e); }
    }


    // --- IReportService Implementation ---

    /**
     * Generates a booking report based on filters.
     * Report includes applicant and flat booking details.
     *
     * @param filters Map of filter criteria (e.g., "maritalStatus", "flatType", "minAge", "startDate", "endDate").
     * @return List of objects representing report data (e.g., enhanced Application or dedicated ReportItem objects). Using Application for now.
     * @throws Exception If report generation fails.
     */
    @Override
    public List<Object> generateBookingReport(Map<String, String> filters) throws Exception {
        // 1. Get all 'BOOKED' applications (or maybe applications linked to receipts?)
        // List<Application> bookedApps = applicationRepo.findByStatus(ApplStatus.BOOKED); // Example repo call
        List<Application> bookedApps = findApplicationsByStatusPlaceholder(ApplStatus.BOOKED); // Placeholder

        List<Object> reportItems = new ArrayList<>();
        if(bookedApps == null || bookedApps.isEmpty()) return reportItems;


        // 2. Apply filters
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); sdf.setLenient(false);
        Date filterStartDate = null, filterEndDate = null;
        try {
            if (filters.containsKey("startDate")) filterStartDate = sdf.parse(filters.get("startDate"));
            if (filters.containsKey("endDate")) {
                filterEndDate = sdf.parse(filters.get("endDate"));
                // Make end date inclusive
                Calendar c = Calendar.getInstance(); c.setTime(filterEndDate); c.add(Calendar.DATE, 1); filterEndDate = c.getTime();
            }
        } catch (ParseException e) { throw new IllegalArgumentException("Invalid date format in filters."); }


        for (Application app : bookedApps) {
            boolean match = true;

            // Find associated applicant data
            Applicant applicant = (Applicant) findUserByNricPlaceholder(app.getApplicantId()); // Placeholder
            if(applicant == null) continue; // Skip if applicant data missing

            // Find associated project data
            Project project = findProjectByIdPlaceholder(app.getProjectId()); // Placeholder
            if(project == null) continue; // Skip if project data missing

            // Apply filters
            if(filters.containsKey("maritalStatus") && (applicant.getMaritalStatus() == null || !applicant.getMaritalStatus().name().equalsIgnoreCase(filters.get("maritalStatus")))) match = false;
            if(match && filters.containsKey("flatType") && (app.getFlatType() == null || !app.getFlatType().equalsIgnoreCase(filters.get("flatType")))) match = false;
            if(match && filters.containsKey("minAge")) {
                try {
                    int age = calculateAge(applicant.getDob());
                    int minAge = Integer.parseInt(filters.get("minAge"));
                    if (age < minAge) match = false;
                } catch (NumberFormatException | NullPointerException e) { match = false; }
            }
            // Apply date filter (based on what date? Application date? Booking date? Need receipt date)
            // Assuming filter applies to application date for now - needs clarification
            // Date appDate = findApplicationDatePlaceholder(app.getId()); // Need application/booking date
            // if (match && filterStartDate != null && appDate.before(filterStartDate)) match = false;
            // if (match && filterEndDate != null && !appDate.before(filterEndDate)) match = false;

            // Filter by Project Name
            if(match && filters.containsKey("projectName") && (project.getProjName() == null || !project.getProjName().equalsIgnoreCase(filters.get("projectName")))) match = false;

            // Filter by Neighbourhood
            if(match && filters.containsKey("neighbourhood") && (project.getNeighbourhood() == null || !project.getNeighbourhood().equalsIgnoreCase(filters.get("neighbourhood")))) match = false;



            if(match) {
                // Create a report item (could be a custom object or just add the Application/Applicant/Project)
                // For simplicity, just adding the Application object now.
                // A dedicated ReportItem class holding combined info would be better.
                reportItems.add(app); // Add application, caller needs to fetch related data
            }
        }

        return reportItems;
    }


    // --- IEnquiryViewable Implementation --- (Inherited, delegates to specific manager methods if needed)
    @Override public List<Enquiry> viewEnquiriesByProject(UUID projectId) { return viewEnquiriesByProject(projectId, null); } // Assuming null manager implies general access
    @Override public List<Enquiry> viewEnquiriesByApplicant(String applicantId) { /* Needs implementation if managers need this view */ return Collections.emptyList(); }
    @Override public List<Enquiry> viewAllEnquiries() { return viewAllEnquiries(null); }
    @Override public Enquiry viewEnquiryById(String enquiryId) { return viewAnyEnquiryById(enquiryId, null); }



    // --- Placeholder methods --- //TODO REMOVEE

    private List<Project> findProjectsByManagerPlaceholder(String managerId) {
        List<Project> all = findAllProjectsPlaceholder();
        List<Project> managed = new ArrayList<>();
        for(Project p : all) {
            if(p.getManager() != null && p.getManager().getId().equals(managerId)) {
                managed.add(p);
            } else if (p.getManager() == null && "T1111111C".equals(managerId) && p.getProjectId().equals("P1001")){
                // Assign P1001 to manager T1111111C for simulation
                HdbManager mgr = (HdbManager) findUserByNricPlaceholder(managerId);
                p.setManager(mgr);
                managed.add(p);
            }
        }
        return managed;
    }

    private String viewOfficerRegistrationStatusPlaceholder(String officerId, String projectId){
        // Simulate checking registration status
        return "PENDING"; // Assume pending for now
    }

    private Application findApplicationByIdPlaceholder(String appId) {
        // Simulate finding app by ID
        // Find in PENDING
        List<Application> pending = findApplicationsByStatusPlaceholder(ApplStatus.PENDING);
        if (pending != null) {
            for (Application app : pending) if (app.getId().equals(appId)) return app;
        }
        // Find in WITHDRAW_PENDING
        List<Application> withdraw = findApplicationsByStatusPlaceholder(ApplStatus.WITHDRAW_PENDING);
        if (withdraw != null) {
            for (Application app : withdraw) if (app.getId().equals(appId)) return app;
        }
        // Find in SUCCESS
        List<Application> success = findApplicationsByStatusPlaceholder(ApplStatus.SUCCESS);
        if (success != null) {
            for (Application app : success) if (app.getId().equals(appId)) return app;
        }
        // Find in BOOKED
        List<Application> booked = findApplicationsByStatusPlaceholder(ApplStatus.BOOKED);
        if (booked != null) {
            for (Application app : booked) if (app.getId().equals(appId)) return app;
        }

        return null;
    }

    private List<Application> findApplicationsByProjectPlaceholder(String projectId){
        // Simulate finding applications by project
        return Collections.emptyList(); // Assume no applications for now
    }

    private List<Application> findApplicationsByStatusPlaceholder(ApplStatus status){
        // Simulate finding applications by status
        return Collections.emptyList(); // Assume no applications for now
    }


    private void deleteProjectPlaceholder(String projectId) throws Exception {
        System.out.println("Placeholder: Deleting project ID " + projectId);
    }

    // --- Inherited Placeholders ---
    // Uses placeholders from UserService and potentially ApplicantService/HdbOfficerService
    // Example: findUserByNricPlaceholder, saveUserPlaceholder, findAllProjectsPlaceholder,
    // findProjectByIdPlaceholder, saveProjectPlaceholder, findAllEnquiriesPlaceholder,
    // findEnquiriesByProjectPlaceholder, findEnquiryByIdPlaceholder, saveEnquiryPlaceholder,
    // saveApplicationPlaceholder etc.
    // Ensure calculateAge is accessible (it's private in ApplicantService, should be protected or moved)
    private int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        LocalDate today = LocalDate.now();
        LocalDate birthday = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthday, today).getYears();
    }


}