package entity;


public class Enquiry {
    private String enquiryId;
    private String applicantId;
    private String projectId;
    private String message;
    private String reply;

    public Enquiry(String enquiryId, String applicantId, String projectId, String message, String reply) {
        this.enquiryId = enquiryId;
        this.applicantId = applicantId;
        this.projectId = projectId;
        this.message = message;
        this.reply = reply;
    }

    public String getEnquiryId() {
        return enquiryId;
    }

    public void setEnquiryId(String enquiryId) {
        this.enquiryId = enquiryId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
