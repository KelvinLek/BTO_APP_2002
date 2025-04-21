package entity;

import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.util.Date;

public class User {
    private String name;
    private String id;
    private Date dob;
    private MaritalStatus maritalStatus;
    private String password;
    private Role role;

    public User(String name, String id, Date dob, MaritalStatus maritalStatus, String password, Role role) {
        this.name = name;
        this.id = id;
        this.dob = dob;
        this.maritalStatus = maritalStatus;
        this.password = password;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge(){
        //TODO implement getAge
        return 0;
    }
}
