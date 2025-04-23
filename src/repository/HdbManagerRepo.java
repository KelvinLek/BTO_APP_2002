package repository;

import entity.HdbManager;
import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HdbManagerRepo {

    private static final String FILE_PATH = "data/ManagerList.csv";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
    private final Map<String, HdbManager> managersMap = new HashMap<>();

    public HdbManagerRepo() {
        loadFromCsv();
    }

    // ==================== Authentication ====================
    public HdbManager authenticate(String nric, String password) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\|");
                if (tokens.length >= 6) {
                    try {
                        String fileNric = tokens[0].trim();
                        String name = tokens[1].trim();
                        String dobStr = tokens[2].trim();
                        String maritalStatusStr = tokens[3].trim();
                        // tokens[4] is Role (ignored)
                        String filePassword = tokens[5].trim();

                        Date dob = sdf.parse(dobStr);

                        if (fileNric.equalsIgnoreCase(nric) && filePassword.equals(password)) {
                            MaritalStatus maritalStatus = MaritalStatus.valueOf(maritalStatusStr.toUpperCase());
                            return new HdbManager(name, fileNric, dob, maritalStatus, filePassword, Role.HDBMANAGER, new ArrayList<>());
                        }

                    } catch (ParseException | IllegalArgumentException e) {
                        System.out.println("Login error (Manager): " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Login error (Manager): " + e.getMessage());
        }
        return null;
    }

    // ==================== CSV Sync ====================
    private void loadFromCsv() {
        managersMap.clear();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\|");
                if (tokens.length >= 6) {
                    try {
                        String nric = tokens[0].trim();
                        String name = tokens[1].trim();
                        Date dob = sdf.parse(tokens[2].trim());
                        MaritalStatus maritalStatus = MaritalStatus.valueOf(tokens[3].trim().toUpperCase());
                        // tokens[4] is Role (ignored)
                        String password = tokens[5].trim();

                        HdbManager manager = new HdbManager(name, nric, dob, maritalStatus, password, Role.HDBMANAGER, new ArrayList<>());
                        managersMap.put(nric, manager);
                    } catch (ParseException | IllegalArgumentException e) {
                        System.out.println("Skipping invalid Manager CSV row: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load managers: " + e.getMessage());
        }
    }

    private void saveToCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            writer.write("NRIC|Name|DOB|MaritalStatus|Role|Password");
            writer.newLine();
            for (HdbManager m : managersMap.values()) {
                writer.write(String.join("|",
                        m.getId(),
                        m.getName(),
                        sdf.format(m.getDob()),
                        m.getMaritalStatus().name(),
                        m.getRole().name(),
                        m.getPassword()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to save managers: " + e.getMessage());
        }
    }

    // ==================== Business Operations ====================
    public void add(HdbManager manager) {
        managersMap.put(manager.getId(), manager);
        saveToCsv();
    }

    public Optional<HdbManager> findById(String id) {
        return Optional.ofNullable(managersMap.get(id));
    }

    public List<HdbManager> findAll() {
        return new ArrayList<>(managersMap.values());
    }

    public void update(HdbManager manager) {
        if (managersMap.containsKey(manager.getId())) {
            managersMap.put(manager.getId(), manager);
            saveToCsv();
        }
    }

    public void delete(String id) {
        managersMap.remove(id);
        saveToCsv();
    }
}