package entity;

import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Applicant extends User{
    private Application application;
    private List<Enquiry> enquiries;

    public Applicant(String name,String id, Date dob, MaritalStatus maritalStatus, String password, Role role, Application application, List<Enquiry> enquiries) {
        super(name, id,dob, maritalStatus, password, role);
        this.application = application;
        this.enquiries = enquiries;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<Enquiry> getEnquiries() {
        return enquiries;
    }

    public void setEnquiries(List<Enquiry> enquiries) {
        this.enquiries = enquiries;
    }

    public void addEnquiry(Enquiry enquiry) {
        if (this.enquiries == null) {
            this.enquiries = new ArrayList<>();
        }
        this.enquiries.add(enquiry);
    }

    public void removeEnquiry(Enquiry enquiry) {
        //TODO implement this
    }
}
