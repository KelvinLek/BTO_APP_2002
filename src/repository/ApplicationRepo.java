package repository;

import entity.Application;
import pub_enums.ApplStatus;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationRepo {
    private final Map<String, Application> applicationsMap = new HashMap<>();
    private static final String APPLICATION_FILE = "data/ApplicationList.csv";
    private static final String DELIMITER = "|";

    public ApplicationRepo() {
        loadFromCsv();
    }

    // ========== CSV File Operations ==========
    private void loadFromCsv() {
        File file = new File(APPLICATION_FILE);
        if (!file.exists()) {
            // Create file if it doesn't exist
            try {
                file.createNewFile();
                try (PrintWriter pw = new PrintWriter(new FileWriter(APPLICATION_FILE))) {
                    pw.println(String.join(DELIMITER, "ID", "Status", "ApplicantID", "ProjectID", "FlatType"));
                }
            } catch (IOException e) {
                System.err.println("Error creating application file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(APPLICATION_FILE))) {
            // Skip header
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                Application application = parseCsvLine(line);
                if (application != null) {
                    applicationsMap.put(application.getId(), application);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CSV: " + e.getMessage());
        }
    }

    private void saveToCsv() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPLICATION_FILE))) {
            // Write header
            pw.println(String.join(DELIMITER, "ID", "Status", "ApplicantID", "ProjectID", "FlatType"));

            // Write data
            for (Application application : applicationsMap.values()) {
                pw.println(toCsvLine(application));
            }
        } catch (IOException e) {
            System.err.println("Error saving CSV: " + e.getMessage());
        }
    }

    // ========== CSV Parsing/Formatting ==========
    private Application parseCsvLine(String line) {
        String[] parts = line.split("\\" + DELIMITER, -1);

        try {
            String id = unescapeCsv(parts[0]);
            ApplStatus status = ApplStatus.valueOf(unescapeCsv(parts[1]));
            String applicantId = unescapeCsv(parts[2]);
            String projectId = unescapeCsv(parts[3]);
            String flatType = unescapeCsv(parts[4]);
            
            return new Application(id, status, applicantId, projectId, flatType);
        } catch (Exception e) {
            System.out.println("Error parsing application data: " + e.getMessage());
            return null;
        }
    }

    private String toCsvLine(Application application) {
        return String.join(DELIMITER,
                escapeCsv(application.getId()),
                escapeCsv(application.getStatus().name()),
                escapeCsv(application.getApplicantId()),
                escapeCsv(application.getProjectId()),
                escapeCsv(application.getFlatType())
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
    public void add(Application application) {
        applicationsMap.put(application.getId(), application);
        saveToCsv();
    }

    public Optional<Application> findById(String id) {
        return Optional.ofNullable(applicationsMap.get(id));
    }

    public List<Application> findAll() {
        return new ArrayList<>(applicationsMap.values());
    }

    public List<Application> findByApplicantId(String applicantId) {
        return applicationsMap.values().stream()
                .filter(app -> app.getApplicantId().equals(applicantId))
                .collect(Collectors.toList());
    }

    public List<Application> findByProjectId(String projectId) {
        return applicationsMap.values().stream()
                .filter(app -> app.getProjectId().equals(projectId))
                .collect(Collectors.toList());
    }

    public Application findActiveByApplicantId(String applicantId) {
        return applicationsMap.values().stream()
                .filter(app -> app.getApplicantId().equals(applicantId))
                .filter(app -> app.getStatus() == ApplStatus.PENDING || 
                              app.getStatus() == ApplStatus.SUCCESS)
                .findFirst()
                .orElse(null);
    }

    public void update(Application application) {
        if (applicationsMap.containsKey(application.getId())) {
            applicationsMap.put(application.getId(), application);
            saveToCsv();
        }
    }

    public void delete(String id) {
        applicationsMap.remove(id);
        saveToCsv();
    }
}