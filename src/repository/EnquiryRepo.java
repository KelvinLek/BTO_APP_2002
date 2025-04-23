package repository;

import entity.Enquiry;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class EnquiryRepo {
    private final Map<String, Enquiry> enquiriesMap = new HashMap<>();
    private static final String ENQUIRY_FILE = "data/EnquiryList.csv";
    private static final String DELIMITER = "|";

    public EnquiryRepo() {
        loadFromCsv();
    }

    // ========== CSV File Operations ==========
    private void loadFromCsv() {
        File file = new File(ENQUIRY_FILE);
        if (!file.exists()) {
            // Create file if it doesn't exist
            try {
                file.createNewFile();
                try (PrintWriter pw = new PrintWriter(new FileWriter(ENQUIRY_FILE))) {
                    pw.println(String.join(DELIMITER, "ID", "ApplicantID", "ProjectID", "Message", "Reply"));
                }
            } catch (IOException e) {
                System.err.println("Error creating enquiry file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ENQUIRY_FILE))) {
            // Skip header
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                Enquiry enquiry = parseCsvLine(line);
                if (enquiry != null) {
                    enquiriesMap.put(enquiry.getEnquiryId(), enquiry);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CSV: " + e.getMessage());
        }
    }

    private void saveToCsv() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ENQUIRY_FILE))) {
            // Write header
            pw.println(String.join(DELIMITER, "ID", "ApplicantID", "ProjectID", "Message", "Reply"));

            // Write data
            for (Enquiry enquiry : enquiriesMap.values()) {
                pw.println(toCsvLine(enquiry));
            }
        } catch (IOException e) {
            System.err.println("Error saving CSV: " + e.getMessage());
        }
    }

    // ========== CSV Parsing/Formatting ==========
    private Enquiry parseCsvLine(String line) {
        String[] parts = line.split("\\" + DELIMITER, -1);

        try {
            String id = unescapeCsv(parts[0]);
            String applicantId = unescapeCsv(parts[1]);
            String projectId = unescapeCsv(parts[2]);
            String message = unescapeCsv(parts[3]);
            String reply = "NULL".equals(parts[4]) ? null : unescapeCsv(parts[4]);
            
            return new Enquiry(id, applicantId, projectId, message, reply);
        } catch (Exception e) {
            System.out.println("Error parsing enquiry data: " + e.getMessage());
            return null;
        }
    }

    private String toCsvLine(Enquiry enquiry) {
        return String.join(DELIMITER,
                escapeCsv(enquiry.getEnquiryId()),
                escapeCsv(enquiry.getApplicantId()),
                escapeCsv(enquiry.getProjectId()),
                escapeCsv(enquiry.getMessage()),
                enquiry.getReply() != null ? escapeCsv(enquiry.getReply()) : "NULL"
        );
    }

    // ========== Helper Methods ==========
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace(DELIMITER, "\\" + DELIMITER)
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String unescapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\\" + DELIMITER, DELIMITER)
                .replace("\\n", "\n")
                .replace("\\r", "\r");
    }

    // ========== Business Operations ==========
    public void add(Enquiry enquiry) {
        enquiriesMap.put(enquiry.getEnquiryId(), enquiry);
        saveToCsv();
    }

    public Optional<Enquiry> findById(String id) {
        return Optional.ofNullable(enquiriesMap.get(id));
    }

    public List<Enquiry> findAll() {
        return new ArrayList<>(enquiriesMap.values());
    }

    public List<Enquiry> findByApplicantId(String applicantId) {
        return enquiriesMap.values().stream()
                .filter(enquiry -> enquiry.getApplicantId().equals(applicantId))
                .collect(Collectors.toList());
    }

    public List<Enquiry> findByProjectId(String projectId) {
        return enquiriesMap.values().stream()
                .filter(enquiry -> enquiry.getProjectId().equals(projectId))
                .collect(Collectors.toList());
    }

    public void update(Enquiry enquiry) {
        if (enquiriesMap.containsKey(enquiry.getEnquiryId())) {
            enquiriesMap.put(enquiry.getEnquiryId(), enquiry);
            saveToCsv();
        }
    }

    public void delete(String id) {
        enquiriesMap.remove(id);
        saveToCsv();
    }
}