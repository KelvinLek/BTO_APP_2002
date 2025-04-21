package service;

import entity.Enquiry;
import entity.HdbOfficer;
import entity.Project; // Needed for context

import java.util.List;


/**
 * Defines enquiry viewing capabilities specifically for HDB Officers.
 * Allows viewing enquiries related to their assigned projects.
 */
public interface IOfficerEnquiryView {

    /**
     * Retrieves all enquiries associated with a specific project that the officer is assigned to.
     *
     * @param projectId The ID of the project.
     * @param officer   The HdbOfficer viewing the enquiries (for permission check).
     * @return A List of Enquiry objects for that project. Returns empty list if none found or officer not assigned.
     * @throws SecurityException if the officer is not assigned to this project.
     */
    List<Enquiry> viewEnquiriesByAssignedProject(String projectId, HdbOfficer officer) throws SecurityException;

    /**
     * Retrieves a single enquiry by its ID, only if it belongs to a project the officer is assigned to.
     *
     * @param enquiryId The ID of the enquiry.
     * @param officer   The HdbOfficer requesting the view.
     * @return The Enquiry object, or null if not found or not accessible by the officer.
     * @throws SecurityException if the officer cannot view this enquiry.
     */
    Enquiry viewAssignedEnquiryById(String enquiryId, HdbOfficer officer) throws SecurityException;
}