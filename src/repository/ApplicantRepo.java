package repository;

import entity.Applicant;
import pub_enums.MaritalStatus;
import pub_enums.Role;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Repository class to handle loading and authenticating applicants from a CSV file.
 */
public class ApplicantRepo {

    private static final String FILE_PATH = "data/ApplicantList.csv";

    /**
     * Authenticates an applicant using NRIC and password from the CSV file.
     *
     * @param nric     NRIC entered by user
     * @param password Password entered by user
     * @return the matched Applicant object if found; null otherwise
     */
    public Applicant authenticate(String nric, String password) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 5) {
                    try {
                        String name = tokens[0].trim();
                        String fileNric = tokens[1].trim();
                        String dobStr = tokens[2].trim(); // Format: dd MM yyyy
                        String maritalStatusStr = tokens[3].trim();
                        String filePassword = tokens[4].trim();

                        // Parse DOB
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
                        Date dob = sdf.parse(dobStr);

                        // Match credentials
                        if (fileNric.equalsIgnoreCase(nric) && filePassword.equals(password)) {
                            MaritalStatus maritalStatus = MaritalStatus.valueOf(maritalStatusStr.toUpperCase());

                            return new Applicant(
                                    name,
                                    fileNric,
                                    dob,
                                    maritalStatus,
                                    filePassword,
                                    Role.APPLICANT,
                                    null,                   // No application on login
                                    new ArrayList<>()       // Empty enquiries
                            );
                        }
                    } catch (ParseException e) {
                        System.out.println("Login error: Invalid DOB format in CSV for NRIC " + tokens[1] + ": " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Login error: Invalid marital status or other enum for NRIC " + tokens[1] + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null; // No match found
    }
}