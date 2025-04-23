package repository;

import entity.HdbOfficer;
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

public class HdbOfficerRepo {

    private static final String FILE_PATH = "data/OfficerList.csv";

    public HdbOfficer authenticate(String nric, String password) {
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

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
                        Date dob = sdf.parse(dobStr);

                        if (fileNric.equalsIgnoreCase(nric) && filePassword.equals(password)) {
                            MaritalStatus maritalStatus = MaritalStatus.valueOf(maritalStatusStr.toUpperCase());

                            return new HdbOfficer(
                                    name,
                                    fileNric,
                                    dob,
                                    maritalStatus,
                                    filePassword,
                                    Role.HDBOFFICER,
                                    null,
                                    new ArrayList<>(),
                                    new ArrayList<>()
                            );
                        }
                    } catch (ParseException e) {
                        System.out.println("Login error (Officer): Invalid DOB format for NRIC " + tokens[1] + ": " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Login error (Officer): Invalid enum value for NRIC " + tokens[1] + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Login error (Officer): " + e.getMessage());
        }

        return null;
    }
}