package util;

import entity.*;
import pub_enums.ApplStatus;
import pub_enums.FlatType;
import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class providing placeholder data access methods for development.
 * In a real implementation, these would be replaced by actual database/repository calls.
 */
public class PlaceholderDataUtil {

    public static List<Project> findAllProjectsPlaceholder() {
        // Simulate finding all projects
        List<Project> projects = new ArrayList<>();
        // Project 1 (Visible, Open)
        Project p1 = new Project("SkyVista @ Dawson", "P1001", "Queenstown", new Date(System.currentTimeMillis() - 86400000 * 5), new Date(System.currentTimeMillis() + 86400000 * 10), null, null);
        p1.setVisible(true);
        List<Flat> p1Flats = new ArrayList<>();
        p1Flats.add(new Flat(FlatType.TWOROOM, 50, 10, 150000));
        p1Flats.add(new Flat(FlatType.THREEROOM, 100, 20, 300000));
        p1.setFlats(p1Flats);
        projects.add(p1);

        // Project 2 (Visible, Closed)
        Project p2 = new Project("Greenery Grove", "P1002", "Tampines", new Date(System.currentTimeMillis() - 86400000 * 30), new Date(System.currentTimeMillis() - 86400000 * 5), null, null);
        p2.setVisible(true);
        List<Flat> p2Flats = new ArrayList<>();
        p2Flats.add(new Flat(FlatType.TWOROOM, 80, 5, 130000));
        p2.setFlats(p2Flats);
        projects.add(p2);

        // Project 3 (Hidden, Open)
        Project p3 = new Project("Woodlands Weave", "P1003", "Woodlands", new Date(System.currentTimeMillis() - 86400000 * 2), new Date(System.currentTimeMillis() + 86400000 * 15), null, null);
        p3.setVisible(false);
        List<Flat> p3Flats = new ArrayList<>();
        p3Flats.add(new Flat(FlatType.THREEROOM, 120, 30, 280000));
        p3.setFlats(p3Flats);
        projects.add(p3);

        return projects;
    }

    public static Project findProjectByIdPlaceholder(String projectId) {
        for(Project p : findAllProjectsPlaceholder()){
            if(p.getProjectId().equals(projectId)) return p;
        }
        return null;
    }

    public static Application findActiveApplicationByApplicantPlaceholder(String applicantId) {
        // Simulate finding an active application (PENDING, SUCCESSFUL, WITHDRAW_PENDING, BOOKED?)
        // This user has no active app
        if ("S1234567A".equalsIgnoreCase(applicantId)) return null;
        // This user might have one - check status
        // Return null for now
        return null;
    }
    
    public static Application findApplicationByIdPlaceholder(String appId) {
        // Placeholder implementation
        if ("A1001".equals(appId)) {
            return new Application(appId, ApplStatus.PENDING, "S1234567A", "P1001", "TWOROOM");
        } else if ("A1002".equals(appId)) {
            return new Application(appId, ApplStatus.SUCCESS, "S7654321B", "P1002", "THREEROOM");
        } else if ("A1003".equals(appId)) {
            return new Application(appId, ApplStatus.WITHDRAW_PENDING, "S1111111C", "P1001", "TWOROOM");
        }
        return null;
    }

    public static Enquiry findEnquiryByIdPlaceholder(String enquiryId) {
        // Simulate finding an enquiry
        if ("E9001".equals(enquiryId)){
            return new Enquiry(enquiryId, "S1234567A", "P1001", "When is the completion date?", null);
        }
        return null;
    }

    public static List<Enquiry> findEnquiriesByApplicantPlaceholder(String applicantId) {
        List<Enquiry> enquiries = new ArrayList<>();
        if ("S1234567A".equalsIgnoreCase(applicantId)) {
            enquiries.add(new Enquiry("E9001", applicantId, "P1001", "When is the completion date?", null));
            enquiries.add(new Enquiry("E9002", applicantId, "P1001", "Can I choose my unit?", "Unit selection is later."));
        }
        return enquiries;
    }
    
    public static List<Enquiry> findEnquiriesByProjectPlaceholder(String projectId) {
        List<Enquiry> enquiries = new ArrayList<>();
        if ("P1001".equals(projectId)) {
            enquiries.add(new Enquiry("E9001", "S1234567A", projectId, "When is the completion date?", null));
            enquiries.add(new Enquiry("E9002", "S1234567A", projectId, "Can I choose my unit?", "Unit selection is later."));
        } else if ("P1003".equals(projectId)) {
            enquiries.add(new Enquiry("E9003", "S9999999Z", projectId, "Is parking included?", null));
        }
        return enquiries;
    }
    
    public static List<Application> findApplicationsByStatusPlaceholder(ApplStatus status) {
        List<Application> applications = new ArrayList<>();
        
        if (status == ApplStatus.PENDING) {
            applications.add(new Application("A1001", ApplStatus.PENDING, "S1234567A", "P1001", "TWOROOM"));
            applications.add(new Application("A1005", ApplStatus.PENDING, "S8888888D", "P1003", "TWOROOM"));
        } else if (status == ApplStatus.WITHDRAW_PENDING) {
            applications.add(new Application("A1003", ApplStatus.WITHDRAW_PENDING, "S1111111C", "P1001", "TWOROOM"));
        }
        
        return applications;
    }

    public static void saveProjectPlaceholder(Project project) {
        System.out.println("Placeholder: Saving project " + project.getProjectId());
    }

    public static void saveApplicationPlaceholder(Application app) throws Exception {
        System.out.println("Placeholder: Saving application ID " + app.getId());
    }

    public static void saveEnquiryPlaceholder(Enquiry enquiry) throws Exception {
        System.out.println("Placeholder: Saving enquiry ID " + enquiry.getEnquiryId());
    }

    public static void deleteEnquiryPlaceholder(String enquiryId) throws Exception {
        System.out.println("Placeholder: Deleting enquiry ID " + enquiryId);
    }
    
    public static void saveUserPlaceholder(User user) {
        System.out.println("Placeholder: Saving user data for " + user.getId());
    }
    
    public static User findUserByNricPlaceholder(String nric) {
        // Placeholder implementation
        if ("S1234567A".equalsIgnoreCase(nric)) {
            // Use dob=null for simplicity, replace with actual Date
            return new Applicant("Alice Tan", nric, null, MaritalStatus.SINGLE, "password", Role.APPLICANT, null, null);
        } else if ("S7654321B".equalsIgnoreCase(nric)) {
            // Use dob=null for simplicity, replace with actual Date
            // HdbOfficer extends Applicant in the provided entity structure
            return new HdbOfficer("Bob Lim", nric, null, MaritalStatus.MARRIED, "password", Role.HDBOFFICER, null, null, null);
        } else if ("T1111111C".equalsIgnoreCase(nric)) {
            // Use dob=null for simplicity, replace with actual Date
            return new HdbManager("Charlie Lee", nric, null, MaritalStatus.MARRIED, "password", Role.HDBMANAGER, null);
        }
        return null;
    }
}