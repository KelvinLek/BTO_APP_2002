package repository;

import entity.HdbOfficer;
import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HdbOfficerRepo {

    private static final String FILE_PATH = "data/OfficerList.csv";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
    private final Map<String, HdbOfficer> officersMap = new HashMap<>();

    public HdbOfficerRepo() {
        loadFromCsv();
    }

    // ==================== Authentication ====================
    public HdbOfficer authenticate(String nric, String password) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\|");
                if (tokens.length >= 7) {
                    try {
                        String fileNric = tokens[0].trim();
                        String name = tokens[1].trim();
                        String dobStr = tokens[2].trim();
                        String maritalStatusStr = tokens[3].trim();
                        // tokens[4] is Role (ignored)
                        String filePassword = tokens[5].trim();
                        // tokens[6] is Status (ignored unless needed)

                        Date dob = sdf.parse(dobStr);

                        if (fileNric.equalsIgnoreCase(nric) && filePassword.equals(password)) {
                            MaritalStatus maritalStatus = MaritalStatus.valueOf(maritalStatusStr.toUpperCase());
                            return new HdbOfficer(name, fileNric, dob, maritalStatus, filePassword, Role.HDBOFFICER, null, new ArrayList<>(), new ArrayList<>());
                        }

                    } catch (ParseException | IllegalArgumentException e) {
                        System.out.println("Login error (Officer): " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Login error (Officer): " + e.getMessage());
        }
        return null;
    }

    // ==================== CSV Sync ====================
    private void loadFromCsv() {
        officersMap.clear();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\|");
                if (tokens.length >= 7) {
                    try {
                        String nric = tokens[0].trim();
                        String name = tokens[1].trim();
                        Date dob = sdf.parse(tokens[2].trim());
                        MaritalStatus maritalStatus = MaritalStatus.valueOf(tokens[3].trim().toUpperCase());
                        // tokens[4] is Role (ignored)
                        String password = tokens[5].trim();
                        // tokens[6] is Status (ignored unless you want to store/use it)

                        HdbOfficer officer = new HdbOfficer(name, nric, dob, maritalStatus, password, Role.HDBOFFICER, null, new ArrayList<>(), new ArrayList<>());
                        officersMap.put(nric, officer);
                    } catch (ParseException | IllegalArgumentException e) {
                        System.out.println("Skipping invalid Officer CSV row: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load officers: " + e.getMessage());
        }
    }

    private void saveToCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            writer.write("ID|Name|DOB|MaritalStatus|Role|Password|Status");
            writer.newLine();
            for (HdbOfficer o : officersMap.values()) {
                writer.write(String.join("|",
                        o.getId(),
                        o.getName(),
                        sdf.format(o.getDob()),
                        o.getMaritalStatus().name(),
                        o.getRole().name(),
                        o.getPassword(),
                        "AVAILABLE")); // Default status if not stored
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to save officers: " + e.getMessage());
        }
    }

    // ==================== Business Operations ====================
    public void add(HdbOfficer officer) {
        officersMap.put(officer.getId(), officer);
        saveToCsv();
    }

    public Optional<HdbOfficer> findById(String id) {
        return Optional.ofNullable(officersMap.get(id));
    }

    public List<HdbOfficer> findAll() {
        return new ArrayList<>(officersMap.values());
    }

    public void update(HdbOfficer officer) {
        if (officersMap.containsKey(officer.getId())) {
            officersMap.put(officer.getId(), officer);
            saveToCsv();
        }
    }

    public void delete(String id) {
        officersMap.remove(id);
        saveToCsv();
    }
}