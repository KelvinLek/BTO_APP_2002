package service;

import entity.Enquiry;
import entity.Project; // Needed for context
import entity.User; // Needed for context

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Defines the contract for managing enquiries related to BTO projects.
 * Covers submission, editing, deletion, and viewing.
 * Reply functionality might be separated (see IReplyable) or included here depending on design.
 */
public interface IEnquiryService {

    /**
     * Submits a new enquiry for a specific project by a user.
     *
     * @param user    The User (Applicant) submitting the enquiry.
     * @param project The Project the enquiry is about.
     * @param message The text content of the enquiry.
     * @return The newly created Enquiry object.
     * @throws NoSuchElementException if user or project is invalid.
     * @throws Exception              if persistence fails.
     */
    Enquiry submitEnquiry(User user, Project project, String message) throws NoSuchElementException, Exception;

    /**
     * Edits the message content of an existing enquiry.
     * Implementations should verify that the user attempting the edit is the owner.
     *
     * @param enquiry   The Enquiry object to be edited (contains ID and potentially owner info).
     * @param newMessage The new message text.
     * @param editor    The User attempting the edit (for permission check).
     * @return true if the edit was successful, false otherwise.
     * @throws NoSuchElementException   if the enquiry is not found.
     * @throws SecurityException        if the editor does not have permission to edit this enquiry.
     * @throws Exception                if persistence fails.
     */
    boolean editEnquiry(Enquiry enquiry, String newMessage, User editor) throws NoSuchElementException, SecurityException, Exception;

    /**
     * Deletes an existing enquiry.
     * Implementations should verify that the user attempting the deletion is the owner.
     *
     * @param enquiryId The ID of the enquiry to delete.
     * @param deleter   The User attempting the deletion (for permission check).
     * @return true if deletion was successful, false otherwise.
     * @throws NoSuchElementException   if the enquiry is not found.
     * @throws SecurityException        if the deleter does not have permission to delete this enquiry.
     * @throws Exception                if persistence fails.
     */
    boolean deleteEnquiry(String enquiryId, User deleter) throws NoSuchElementException, SecurityException, Exception;

    // Note: Viewing methods moved to IEnquiryViewable as per the structure.
    // Reply methods moved to IReplyable as per the structure.
}