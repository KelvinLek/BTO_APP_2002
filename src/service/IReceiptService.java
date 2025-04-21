package service;

import entity.Application; // Needed to verify booking before generating
import entity.Receipt;
import entity.User; // Changed from HdbOfficer, as generation might be triggered by different roles
import java.util.List; // Added for potential reporting uses
import java.util.NoSuchElementException;

/**
 * Defines the contract for generating and potentially retrieving BTO booking receipts.
 */
public interface IReceiptService {

    /**
     * Generates a receipt for a confirmed flat booking.
     * Verifies that the associated application status is "Booked".
     * Creates and persists a new Receipt object.
     *
     * @param application The Application object (must have status "Booked") for which to generate the receipt.
     * @param generatingUser The User (likely HdbOfficer) generating the receipt.
     * @return The newly created Receipt object.
     * @throws NoSuchElementException if the application is not found.
     * @throws IllegalStateException  if the application status is not "Booked".
     * @throws Exception              for underlying persistence errors.
     */
    Receipt generateReceipt(Application application, User generatingUser) throws NoSuchElementException, IllegalStateException, Exception;

    /**
     * Retrieves a receipt associated with a specific application.
     *
     * @param application The application whose receipt is needed.
     * @return The Receipt object, or null if not found.
     * @throws NoSuchElementException if the application is not found.
     */
    Receipt findReceiptForApplication(Application application) throws NoSuchElementException;

    // Consider adding methods to find receipts by applicant or project if needed for reporting
    // List<Receipt> findReceiptsByApplicant(String applicantId);
    // List<Receipt> findReceiptsByProject(UUID projectId);
}