package service;

import entity.Receipt; // Reports are based on booking receipts/details
import java.util.List;
import java.util.Map;

/**
 * Defines the contract for generating reports based on BTO application and booking data.
 * Primarily used by HDB Managers.
 */
public interface IReportService {

    /**
     * Generates a report of applicants and their flat bookings based on specified filters.
     * Filters can include project, flat type, applicant age, marital status, date range etc.
     * The implementation needs to gather data likely from Application and User/Applicant repos.
     *
     * @param filters A Map containing filter criteria (e.g., key="maritalStatus", value="Married"; key="flatType", value="3-Room").
     * @return A List of objects representing the report data (could be custom ReportItem objects or potentially enhanced Receipt/Application objects). Using List<Receipt> as a placeholder.
     * @throws Exception if report generation encounters errors (e.g., data inconsistency).
     */
    List<Object> generateBookingReport(Map<String, String> filters) throws Exception;
    // Changed return type to List<Object> as List<Receipt> might not be sufficient for full report details (e.g., age, marital status). Needs a dedicated ReportItem class or similar.

}