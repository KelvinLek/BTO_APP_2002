package entity;

import java.util.Date;
import java.util.List;

public class Project {
    private String projName;
    private String projId;
    private Boolean visible;
    private String neighbourhood;
    private List<Flat> flats;
    private Date appOpen;
    private Date appClose;
    private HdbManager manager;
    private List<HdbOfficer> officers;
    private Integer officerSlots;

    public Project(String projName, String projId, String neighbourhood, Date appOpen, Date appClose, HdbManager manager, List<HdbOfficer> officers) {
        this.projName = projName;
        this.neighbourhood = neighbourhood;
        this.appOpen = appOpen;
        this.appClose = appClose;
        this.manager = manager;
        this.officers = officers;
        this.projId = projId;
        this.visible = true;
        this.officerSlots = 5; // Default value

        //TODO implement Flat as a composition
    }

    public Project(String projName, String projId, Boolean visible, String neighbourhood, List<Flat> flats, Date appOpen, Date appClose, HdbManager manager, List<HdbOfficer> officers, Integer officerSlots) {
        this.projName = projName;
        this.projId = projId;
        this.visible = visible;
        this.neighbourhood = neighbourhood;
        this.flats = flats;
        this.appOpen = appOpen;
        this.appClose = appClose;
        this.manager = manager;
        this.officers = officers;
        this.officerSlots = officerSlots;

        //TODO implement Flat as a composition
    }

    public Project(String projName, String projId, Boolean visible, String neighbourhood, List<Flat> flats, Date appOpen, Date appClose, HdbManager manager, List<HdbOfficer> officers) {
        this.projName = projName;
        this.projId = projId;
        this.visible = visible;
        this.neighbourhood = neighbourhood;
        this.flats = flats;
        this.appOpen = appOpen;
        this.appClose = appClose;
        this.manager = manager;
        this.officers = officers;
        this.officerSlots = 5; // Default value
    }

    // Constructor for placeholder/test data
    public Project(String projName, String neighbourhood, Date appOpen, Date appClose, HdbManager manager, List<HdbOfficer> officers) {
        this.projName = projName;
        this.neighbourhood = neighbourhood;
        this.appOpen = appOpen;
        this.appClose = appClose;
        this.manager = manager;
        this.officers = officers;
        this.visible = true;
        this.officerSlots = 5; // Default value
    }

    public String getProjName() {
        return projName;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public String getProjectId() {
        return projId;
    }

    public void setProjectId(String projId) {
        this.projId = projId;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public List<Flat> getFlats() {
        return flats;
    }

    public void setFlats(List<Flat> flats) {
        this.flats = flats;
    }

    public Date getAppOpen() {
        return appOpen;
    }

    public void setAppOpen(Date appOpen) {
        this.appOpen = appOpen;
    }

    public Date getAppClose() {
        return appClose;
    }

    public void setAppClose(Date appClose) {
        this.appClose = appClose;
    }

    public HdbManager getManager() {
        return manager;
    }

    public void setManager(HdbManager manager) {
        this.manager = manager;
    }

    public List<HdbOfficer> getOfficers() {
        return officers;
    }

    public void setOfficers(List<HdbOfficer> officers) {
        this.officers = officers;
    }

    public void removeOfficer(HdbOfficer officer) {
        this.officers.remove(officer);
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public Integer getOfficerSlots() {
        return officerSlots;
    }

    public void setOfficerSlots(Integer officerSlots) {
        this.officerSlots = officerSlots;
    }
}