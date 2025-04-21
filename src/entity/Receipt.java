package entity;

import java.util.Date;

public class Receipt {
    private String receiptId;
    private Date issuedDate;
    private String projectId;
    private String applicantId;

    public Receipt(String receiptId, Date issuedDate, String projectId, String applicantId) {
        this.receiptId = receiptId;
        this.issuedDate = issuedDate;
        this.projectId = projectId;
        this.applicantId = applicantId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }
}
