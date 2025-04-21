package service;

import entity.Enquiry;
import entity.HdbManager;

import java.util.List;
import java.util.UUID;

/**
 * Defines enquiry viewing capabilities specifically for HDB Managers.
 * Allows viewing all enquiries.
 */
public interface IManagerEnquiryView {

    /**
     * Retrieves all enquiries in the system.
     *
     * @param manager The HdbManager performing the action (context).
     * @return A List of all Enquiry objects.
     */
    List<Enquiry> viewAllEnquiries(HdbManager manager);

    /**
     * Retrieves all enquiries associated with a specific project.
     *
     * @param projectId The ID of the project.
     * @param manager   The HdbManager performing the action (context).
     * @return A List of Enquiry objects for that project. Returns empty list if none found.
     */
    List<Enquiry> viewEnquiriesByProject(UUID projectId, HdbManager manager);

    /**
     * Retrieves a single enquiry by its unique ID.
     *
     * @param enquiryId The ID of the enquiry.
     * @param manager   The HdbManager performing the action (context).
     * @return The Enquiry object, or null if not found.
     */
    Enquiry viewAnyEnquiryById(String enquiryId, HdbManager manager);

}