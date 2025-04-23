package entity;

import pub_enums.MaritalStatus;
import pub_enums.OfficerStatus;
import pub_enums.Role;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static pub_enums.OfficerStatus.AVAILABLE;

public class HdbOfficer extends Applicant{
    private List<Project> assignedProjects;
    private OfficerStatus status; // Added to track registration status

    public HdbOfficer(String name, String id, Date dob, MaritalStatus maritalStatus, String password, Role role, Application application, List<Enquiry> enquiries, List<Project> assignedProjects) {
        super(name, id, dob, maritalStatus, password, role, application, enquiries);
        this.assignedProjects = assignedProjects;
        this.status = AVAILABLE; // Default status
    }

    public List<Project> getAssignedProjects() {
        return assignedProjects;
    }

    public void setAssignedProjects(List<Project> assignedProjects) {
        this.assignedProjects = assignedProjects;
    }

    public void addAssignedProject(Project project) {
        if (this.assignedProjects == null) {
            this.assignedProjects = new ArrayList<>();
        }
        this.assignedProjects.add(project);
    }

    public void removeAssignedProject(Project project) {
        if (this.assignedProjects != null) {
            this.assignedProjects.remove(project);
        }
    }

    public OfficerStatus getStatus() {
        return status;
    }

    public void setStatus(OfficerStatus status) {
        this.status = status;
    }
}