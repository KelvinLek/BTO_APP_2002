package entity;

import pub_enums.ApplStatus;

public class Application {
    private String id;
    private ApplStatus status;
    private String applicantId;
    private String projectId;
    private String flatType;

    public Application(String id, ApplStatus status, String applicantId, String projectId, String flatType) {
        this.id = id;
        this.status = status;
        this.applicantId = applicantId;
        this.projectId = projectId;
        this.flatType = flatType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ApplStatus getStatus() {
        return status;
    }

    public void setStatus(ApplStatus status) {
        this.status = status;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFlatType() {
        return flatType;
    }

    public void setFlatType(String flatType) {
        this.flatType = flatType;
    }
}
