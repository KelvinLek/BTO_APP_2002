package entity;

import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HdbManager extends User{
    private List<Project> createdProjects;

    public HdbManager(String name, String id, Date dob, MaritalStatus maritalStatus, String password, Role role, List<Project> createdProjects) {
        super(name, id, dob, maritalStatus, password, role);
        this.createdProjects = createdProjects;
    }

    public List<Project> getCreatedProjects() {
        return createdProjects;
    }

    public void setCreatedProjects(List<Project> createdProjects) {
        this.createdProjects = createdProjects;
    }

    public void addCreatedProject(Project project) {
        if (this.createdProjects == null) {
            this.createdProjects = new ArrayList<>();
        }
        this.createdProjects.add(project);
    }

    public void removeCreatedProject(){
        //TODO implement this
    }

}
