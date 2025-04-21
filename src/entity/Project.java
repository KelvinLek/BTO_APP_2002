package entity;

import java.util.Date;
import java.util.List;

public class Project {
    private String projName;
    private String neighbourhood;
    private List<Flat> flats;
    private Date appOpen;
    private Date appClose;
    private HdbManager manager;
    private List<HdbOfficer> officers;

    public Project(String projName, String neighbourhood, Date appOpen, Date appClose, HdbManager manager, List<HdbOfficer> officers) {
        this.projName = projName;
        this.neighbourhood = neighbourhood;
        this.appOpen = appOpen;
        this.appClose = appClose;
        this.manager = manager;
        this.officers = officers;

        //TODO implement Flat as a composition
    }
}
