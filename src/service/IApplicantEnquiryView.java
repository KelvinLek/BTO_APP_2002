package service;

import entity.Applicant;
import entity.Enquiry;
import java.util.List;

/**
 * Defines enquiry viewing capabilities specifically for Applicants.
 * Allows viewing only their own submitted enquiries.
 */
public interface IApplicantEnquiryView {

    /**
     * Retrieves all enquiries submitted by a specific applicant.
     *
     * @param applicant The Applicant whose enquiries are to be viewed.
     * @return A List of Enquiry objects submitted by that applicant. Returns empty list if none found.
     */
    List<Enquiry> viewMyEnquiries(Applicant applicant);

    /**
     * Retrieves a single enquiry by its unique ID, only if it belongs to the applicant.
     *
     * @param enquiryId The ID of the enquiry.
     * @param applicant The Applicant requesting the view.
     * @return The Enquiry object, or null if not found or not owned by the applicant.
     * @throws SecurityException if the enquiry does not belong to the applicant.
     */
    Enquiry viewMyEnquiryById(String enquiryId, Applicant applicant) throws SecurityException;
}