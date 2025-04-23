package repository;

import entity.Applicant;
import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

                            return new Applicant(
                                    name,
                                    fileNric,
                                    dob,
                                    maritalStatus,
                                    filePassword,
                                    Role.APPLICANT,
                                    null,
                                    new ArrayList<>()
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
                String[] tokens = line.split("\\|");
                if (tokens.length >= 6) {
                    try {
                        String nric = tokens[0].trim();
                        String name = tokens[1].trim();
                        Date dob = sdf.parse(tokens[2].trim());
                        MaritalStatus maritalStatus = MaritalStatus.valueOf(tokens[3].trim().toUpperCase());
                        String roleStr = tokens[4].trim(); // Not used but retained for structure
                        String password = tokens[5].trim();

                        Applicant applicant = new Applicant(name, nric, dob, maritalStatus, password, Role.APPLICANT, null, new ArrayList<>());
                        applicantsMap.put(nric, applicant);
                    } catch (ParseException | IllegalArgumentException e) {
                        System.out.println("Skipping invalid row in CSV: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load applicants: " + e.getMessage());
        }
    }

    private void saveToCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            writer.write("ID|Name|DOB|MaritalStatus|Role|Password");
            writer.newLine();

            for (Applicant a : applicantsMap.values()) {
                String dobStr = sdf.format(a.getDob());
                writer.write(String.join("|",
                        a.getId(),
                        a.getName(),
                        dobStr,
                        a.getMaritalStatus().name(),
                        a.getRole().name(),
                        a.getPassword()
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
}