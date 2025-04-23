package entity;

import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public abstract class User {
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

    public int getAge() {
        if (this.dob == null) {
            return 0; // Return 0 if no date of birth is available
        }
        try {
            // Convert java.util.Date to java.time.LocalDate
            LocalDate birthDate = this.dob.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate currentDate = LocalDate.now();

            // Calculate the period between the birth date and current date
            Period period = Period.between(birthDate, currentDate);

            // Return the number of years from the period
            return period.getYears();
        } catch (Exception e) {
            // Handle potential exceptions during date conversion or calculation
            System.err.println("Error calculating age for user " + this.id + ": " + e.getMessage());
            return 0; // Return 0 on error
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
