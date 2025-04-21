package service;

import entity.Application;
import entity.HdbOfficer;
import pub_enums.FlatType;
import java.util.NoSuchElementException;

/**
 * Defines the contract for the flat booking service, performed by HDB Officers
 * for applicants with successful applications.
 */
public interface IBookFlatService {

    /**
     * Books a specific flat type for an applicant within a project.
     * This action should only be possible if the applicant's application status is "Successful".
     * Updates the application status to "Booked".
     * Updates the applicant's profile (if necessary - maybe Application stores booked flat type).
     * Decrements the count of available flats for the booked type in the project.
     *
     * @param application The successful Application for which the booking is made.
     * @param flatType    The specific FlatType being booked.
     * @param officer     The HdbOfficer performing the booking (must be assigned to the project).
     * @return true if the booking was successful, false otherwise.
     * @throws NoSuchElementException   if application, project, or officer not found.
     * @throws IllegalStateException    if the application status is not "Successful", or no units of the flatType are available.
     * @throws SecurityException        if the officer is not assigned/approved for this project.
     * @throws Exception                for underlying persistence errors.
     */
    boolean bookFlat(Application application, FlatType flatType, HdbOfficer officer) throws NoSuchElementException, IllegalStateException, SecurityException, Exception;
}