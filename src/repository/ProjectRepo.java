package repository;

import entity.*;
import pub_enums.FlatType;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

public class ProjectRepo {
    private final Map<String, Project> projectsMap = new HashMap<>();
    private static final String PROJECT_FILE = "data/ProjectList.csv";
    private static final String DELIMITER = "\\|";
    private static final String FLATS_SEPARATOR = ";";
    private static final String OFFICERS_SEPARATOR = ",";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy", Locale.ENGLISH);

    private HdbManagerRepo managerRepo;
    private HdbOfficerRepo officerRepo;

    public ProjectRepo(HdbManagerRepo managerRepo, HdbOfficerRepo officerRepo) {
        this.managerRepo = managerRepo;
        this.officerRepo = officerRepo;
        loadFromCsv();
    }

    // ========== CSV File Operations ==========
    private void loadFromCsv() {
        File file = new File(PROJECT_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(PROJECT_FILE))) {
            // Skip header
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                Project project = parseCsvLine(line);
                if (project != null) {
                    projectsMap.put(project.getProjectId(), project);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveToCsv() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PROJECT_FILE))) {
            // Write header
            pw.println(String.join("|",
                    "ProjectId", "ProjectName", "Visible", "Neighbourhood", 
                    "OpenDate", "CloseDate", "ManagerId", "OfficerSlots", 
                    "OfficerIds", "Flat Details (Type, Total, Remaining, Price)"));

            // Write data
            for (Project project : projectsMap.values()) {
                pw.println(toCsvLine(project));
            }
        } catch (IOException e) {
            System.err.println("Error saving CSV: " + e.getMessage());
        }
    }

    // ========== CSV Parsing/Formatting ==========
    private Project parseCsvLine(String line) {
        String[] parts = line.split("\\|", -1);

        try {
            // Debug output
//            System.out.println("Parsing project line: " + line);
//            System.out.println("Parts length: " + parts.length);
            
            String projectId = parts[0].trim();
            String projectName = parts[1].trim();
            Boolean visible = Boolean.parseBoolean(parts[2].trim());
            String neighbourhood = parts[3].trim();
            Date openDate = parts[4].equals("NULL") ? null : dateFormat.parse(parts[4].trim());
            Date closeDate = parts[5].equals("NULL") ? null : dateFormat.parse(parts[5].trim());
            String managerId = parts[6].trim();
            Integer officerSlots = Integer.parseInt(parts[7].trim());
            
            // Get manager
            HdbManager manager = null;
            if (!managerId.equals("NULL")) {
                manager = managerRepo.findById(managerId).orElse(null);
            }
            
            // Parse officer IDs and get officers
            List<HdbOfficer> officers = new ArrayList<>();
            if (parts.length > 8 && !parts[8].equals("NULL")) {
                String[] officerIds = parts[8].trim().split(OFFICERS_SEPARATOR);
                for (String officerId : officerIds) {
                    HdbOfficer officer = officerRepo.findById(officerId).orElse(null);
                    if (officer != null) {
                        officers.add(officer);
                    }
                }
            }
            
            // Parse flats
            List<Flat> flats = new ArrayList<>();
            if (parts.length > 9 && !parts[9].equals("NULL")) {
                flats = parseFlats(parts[9].trim());
            }
            
            return new Project(
                    projectName,
                    projectId,
                    visible,
                    neighbourhood,
                    flats,
                    openDate,
                    closeDate,
                    manager,
                    officers,
                    officerSlots
            );
        } catch (ParseException e) {
            System.out.println("Error reading project data: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String toCsvLine(Project project) {
        String managerId = project.getManager() != null ? project.getManager().getId() : "NULL";
        String officerIds = formatOfficerIds(project.getOfficers());
        String flats = formatFlats(project.getFlats());
        
        return String.join("|",
                project.getProjectId(),
                project.getProjName(),
                String.valueOf(project.isVisible()),
                project.getNeighbourhood(),
                project.getAppOpen() != null ? dateFormat.format(project.getAppOpen()) : "NULL",
                project.getAppClose() != null ? dateFormat.format(project.getAppClose()) : "NULL",
                managerId,
                String.valueOf(project.getOfficerSlots()),
                officerIds.isEmpty() ? "NULL" : officerIds,
                flats.isEmpty() ? "NULL" : flats
        );
    }

    // ========== Helper Methods ==========
    private List<Flat> parseFlats(String flatsStr) {
        if ("NULL".equals(flatsStr)) return new ArrayList<>();
        
        List<Flat> flats = new ArrayList<>();
        String[] flatEntries = flatsStr.split(FLATS_SEPARATOR);
        
        for (String flatEntry : flatEntries) {
            String[] flatData = flatEntry.split(",");
            if (flatData.length >= 2) {
                FlatType flatType = FlatType.valueOf(flatData[0]);
                int total = Integer.parseInt(flatData[1]);
                int remaining = Integer.parseInt(flatData[2]);
                double price = Double.parseDouble(flatData[3]);
                flats.add(new Flat(flatType, total, remaining, price));
            }
        }
        
        return flats;
    }

    private String formatFlats(List<Flat> flats) {
        if (flats == null || flats.isEmpty()) return "";
        
        return flats.stream()
                .map(f ->  f.getFlatType().name() + "," + f.getTotal() + ","  + f.getRemaining() + ","  + f.getPrice())
                .collect(Collectors.joining(FLATS_SEPARATOR));
    }

    private String formatOfficerIds(List<HdbOfficer> officers) {
        if (officers == null || officers.isEmpty()) return "";
        
        return officers.stream()
                .map(HdbOfficer::getId)
                .collect(Collectors.joining(OFFICERS_SEPARATOR));
    }

    // ========== Business Operations ==========
    public void add(Project project) {
        projectsMap.put(project.getProjectId(), project);
        saveToCsv();
    }

    public Optional<Project> findById(String id) {
        return Optional.ofNullable(projectsMap.get(id));
    }

    public List<Project> findAll() {
        return new ArrayList<>(projectsMap.values());
    }
    
    public List<Project> findByManagerId(String managerId) {
        return projectsMap.values().stream()
                .filter(p -> p.getManager() != null && p.getManager().getId().equals(managerId))
                .collect(Collectors.toList());
    }

    public List<Project> findByOfficerId(String officerId) {
        return projectsMap.values().stream()
                .filter(p -> p.getOfficers() != null && 
                        p.getOfficers().stream().anyMatch(o -> o.getId().equals(officerId)))
                .collect(Collectors.toList());
    }

    public void update(Project project) {
        if (projectsMap.containsKey(project.getProjectId())) {
            projectsMap.put(project.getProjectId(), project);
            saveToCsv();
        }
    }

    public void delete(Project project) {
        projectsMap.remove(project.getProjectId());
        saveToCsv();
    }
}