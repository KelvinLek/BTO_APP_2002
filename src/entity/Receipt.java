package entity;

import java.util.Date;

public class Receipt {
    private String receiptId;
    private Date issuedDate;
    private int projectId;
    private String applicantId;

    public Receipt(String receiptId, Date issuedDate, int projectId, String applicantId) {
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

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }
}
