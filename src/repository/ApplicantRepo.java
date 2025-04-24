package repository;

import entity.Applicant;
import entity.Application;
import entity.Enquiry;
import pub_enums.ApplStatus;
import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class to handle loading, authenticating, and managing applicants from a CSV file.
 */
public class ApplicantRepo {

    private static final String FILE_PATH = "data/ApplicantList.csv";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
    private final Map<String, Applicant> applicantsMap = new HashMap<>();

    public ApplicantRepo() {
        loadFromCsv(); // Load applicants on initialization
    }

    // ========== Authentication ==========
    public Applicant authenticate(String nric, String password) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\|");
                if (tokens.length >= 6) {
                    try {
                        String fileNric = tokens[0].trim();
                        String name = tokens[1].trim();
                        String dobStr = tokens[2].trim();
                        String maritalStatusStr = tokens[3].trim();
                        String roleStr = tokens[4].trim(); // Not used in logic but required in CSV
                        String filePassword = tokens[5].trim();

                        Date dob = sdf.parse(dobStr);

                        if (fileNric.equalsIgnoreCase(nric) && filePassword.equals(password)) {
                            MaritalStatus maritalStatus = MaritalStatus.valueOf(maritalStatusStr.toUpperCase());

                            Application application = parseApplication(tokens.length > 6 ? tokens[6].trim() : "NULL");

                            // Parse enquiries if exist (column 7)
                            List<Enquiry> enquiries = tokens.length > 7 ?
                                    parseEnquiries(tokens[7].trim()) : new ArrayList<>();

                            return new Applicant(
                                    name,
                                    fileNric,
                                    dob,
                                    maritalStatus,
                                    filePassword,
                                    Role.APPLICANT,
                                    application, // Now includes parsed application
                                    enquiries   // Now includes parsed enquiries
                            );
                        }
                    } catch (ParseException | IllegalArgumentException e) {
                        System.out.println("Login error: Invalid CSV entry: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }

    // ========== CSV Sync ==========
    private void loadFromCsv() {
        applicantsMap.clear();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\|", -1); // -1 keeps empty fields
                if (tokens.length >= 6) { // Updated for new fields
                    try {
                        String nric = tokens[0].trim();
                        String name = tokens[1].trim();
                        Date dob = sdf.parse(tokens[2].trim());
                        MaritalStatus maritalStatus = MaritalStatus.valueOf(tokens[3].trim().toUpperCase());
                        String password = tokens[5].trim();

                        // Parse single application (column 6)
                        Application application = parseApplication(tokens.length > 6 ? tokens[6].trim() : "NULL");

                        // Parse enquiries (column 7)
                        List<Enquiry> enquiries = tokens.length > 7 ?
                                parseEnquiries(tokens[7].trim()) : new ArrayList<>();

                        Applicant applicant = new Applicant(
                                name,
                                nric,
                                dob,
                                maritalStatus,
                                password,
                                Role.APPLICANT,
                                application, // Single application
                                enquiries
                        );
                        applicantsMap.put(nric, applicant);
                    } catch (ParseException | IllegalArgumentException e) {
                        System.err.println("Skipping invalid row: " + line + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load applicants: " + e.getMessage());
        }
    }

    private void saveToCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            writer.write("ID|Name|DOB|MaritalStatus|Role|Password|Application|Enquiries");
            writer.newLine();

            for (Applicant applicant : applicantsMap.values()) {
                String dobStr = sdf.format(applicant.getDob());
                String applicationsStr = applicant.getApplication() != null ?
                        formatApplications(Collections.singletonList(applicant.getApplication())) : "NULL";
                String enquiriesStr = formatEnquiries(applicant.getEnquiries());

                writer.write(String.join("|",
                        applicant.getId(),
                        applicant.getName(),
                        dobStr,
                        applicant.getMaritalStatus().name(),
                        applicant.getRole().name(),
                        applicant.getPassword(),
                        applicationsStr,
                        enquiriesStr
                ));
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Failed to save applicants: " + e.getMessage());
        }
    }

    // ========== Business Operations ==========

    public void add(Applicant applicant) {
        applicantsMap.put(applicant.getId(), applicant);
        saveToCsv();
    }

    public Optional<Applicant> findById(String id) {
        return Optional.ofNullable(applicantsMap.get(id));
    }

    public List<Applicant> findAll() {
        return new ArrayList<>(applicantsMap.values());
    }

    public void update(Applicant applicant) {
        if (applicantsMap.containsKey(applicant.getId())) {
            applicantsMap.put(applicant.getId(), applicant);
            saveToCsv();
        }
    }

    public void delete(String id) {
        applicantsMap.remove(id);
        saveToCsv();
    }

    // Helper Functions
    private String formatEnquiries(List<Enquiry> enquiries) {
        if (enquiries == null || enquiries.isEmpty()) return "";

        return enquiries.stream()
                .map(e ->  e.getEnquiryId() + "," + e.getApplicantId() + ","  + e.getProjectId() + ","  + e.getMessage() + "," + e.getReply())
                .collect(Collectors.joining(";"));
    }

    private List<Enquiry> parseEnquiries(String enquiriesStr) {
        if ("NULL".equals(enquiriesStr)) return new ArrayList<>();

        List<Enquiry> enquiries = new ArrayList<>();
        String[] enquiryEntries = enquiriesStr.split(";");

        for (String enquiryEntry : enquiryEntries) {
            String[] enquiryData = enquiryEntry.split(",");
            if (enquiryData.length >= 2) {
                String enquiryId = enquiryData[0];
                String applId = enquiryData[1];
                String projId = enquiryData[2];
                String message = enquiryData[3];
                String reply = enquiryData[4];
                enquiries.add(new Enquiry(enquiryId, applId, projId, message, reply));
            }
        }

        return enquiries;
    }

    private String formatApplications(List<Application> applications) {
        if (applications == null || applications.isEmpty()) return "";

        return applications.stream()
                .map(a ->  a.getId() + "," + String.valueOf(a.getStatus()) + ","  + a.getApplicantId() + ","  + a.getProjectId() + "," + a.getFlatType())
                .collect(Collectors.joining(";"));
    }

    private Application parseApplication(String applicationStr) {
        if ("NULL".equals(applicationStr) || applicationStr == null || applicationStr.trim().isEmpty()) {
            return null;
        }

        String[] applicationData = applicationStr.split(",");
        if (applicationData.length >= 5) {
            return new Application(
                    applicationData[0], // applicationId
                    ApplStatus.valueOf(applicationData[1]), // status
                    applicationData[2], // applicantId
                    applicationData[3], // projectId
                    applicationData[4]  // flatType
            );
        }

        return null;
    }
}