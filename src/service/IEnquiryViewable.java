package service;

import entity.Enquiry;
import java.util.List;
import java.util.UUID;

/**
 * Defines the contract for viewing enquiries.
 * Separated from IEnquiryService based on ISP, focusing on read operations.
 */
public interface IEnquiryViewable {

    /**
     * Retrieves all enquiries associated with a specific project.
     *
     * @param projectId The ID of the project.
     * @return A List of Enquiry objects for that project. Returns empty list if none found.
     */
    List<Enquiry> viewEnquiriesByProject(UUID projectId);

    /**
     * Retrieves all enquiries submitted by a specific applicant.
     *
     * @param applicantId The NRIC/ID of the applicant.
     * @return A List of Enquiry objects submitted by that applicant. Returns empty list if none found.
     */
    List<Enquiry> viewEnquiriesByApplicant(String applicantId);

    /**
     * Retrieves all enquiries in the system.
     * Typically used by HDB Managers. Use with caution due to potential volume.
     *
     * @return A List of all Enquiry objects.
     */
    List<Enquiry> viewAllEnquiries();

    /**
     * Retrieves a single enquiry by its unique ID.
     *
     * @param enquiryId The ID of the enquiry.
     * @return The Enquiry object, or null if not found.
     */
    Enquiry viewEnquiryById(String enquiryId); // Added for completeness

}