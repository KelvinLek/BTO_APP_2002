package service;

import entity.Application;
import entity.Enquiry;
import entity.Project;
import entity.User;

import java.util.Date;
import java.util.List;

/**
 * Defines application-related operations for users who can apply for projects and submit enquiries.
 */
public interface IApplyableService {

    /**
     * Calculates the age of a user based on their date of birth.
     *
     * @param birthDate The user's date of birth.
     * @return The age in years.
     */
    int calculateAge(Date birthDate);

    /**
     * Applies for a project.
     *
     * @param user The User applying.
     * @param project The Project being applied for.
     * @return true if the application was successful, false otherwise.
     */
    boolean applyForProject(User user, Project project);

    /**
     * Retrieves the current application status for a user.
     *
     * @param user The User.
     * @return The current Application object, or null if none found.
     */
    Application getApplicationStatus(User user);

    /**
     * Submits a new enquiry for a project.
     *
     * @param user The User submitting the enquiry.
     * @param project The Project the enquiry is about.
     * @param message The enquiry message.
     * @return The created Enquiry object.
     */
    Enquiry submitEnquiry(User user, Project project, String message);

    /**
     * Retrieves all enquiries submitted by a user.
     *
     * @param user The User.
     * @return A List of Enquiry objects.
     */
    List<Enquiry> viewEnquiries(User user);

    /**
     * Retrieves a specific enquiry by ID, ensuring it's accessible to the user.
     *
     * @param enquiryId The ID of the enquiry.
     * @param user The User requesting the view.
     * @return The Enquiry object or null if not found/not authorized.
     */
    Enquiry viewEnquiryById(String enquiryId, User user);

    /**
     * Edits an existing enquiry.
     *
     * @param enquiryId The ID of the enquiry to edit.
     * @param user The User attempting to edit.
     * @param newMessage The new message.
     */
    void editEnquiry(String enquiryId, User user, String newMessage);

    /**
     * Deletes an existing enquiry.
     *
     * @param enquiryId The ID of the enquiry to delete.
     * @param user The User attempting to delete.
     */
    void deleteEnquiry(String enquiryId, User user);
}