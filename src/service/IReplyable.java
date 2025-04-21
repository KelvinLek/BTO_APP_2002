package service;

import entity.Enquiry;
import entity.User; // User replying could be Officer or Manager
import java.util.NoSuchElementException;

/**
 * Defines the contract for replying to enquiries.
 * Could potentially be merged into IEnquiryService or role-specific services (Officer/Manager),
 * but defined separately as per the BTO_App_2 structure.
 */
public interface IReplyable {

    /**
     * Adds a reply message to an existing enquiry.
     * Implementations should check if the replying user has permission (e.g., assigned Officer or Manager).
     *
     * @param enquiry   The Enquiry object to reply to.
     * @param replyText The text content of the reply.
     * @param replyingUser The User (HdbOfficer or HdbManager) submitting the reply.
     * @return true if the reply was successfully saved, false otherwise.
     * @throws NoSuchElementException if the enquiry is not found.
     * @throws SecurityException      if the user does not have permission to reply to this enquiry.
     * @throws Exception              for underlying persistence errors.
     */
    boolean replyToEnquiry(Enquiry enquiry, String replyText, User replyingUser) throws NoSuchElementException, SecurityException, Exception;
    // Renamed parameters for clarity
}