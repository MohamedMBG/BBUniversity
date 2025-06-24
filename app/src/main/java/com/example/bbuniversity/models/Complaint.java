package com.example.bbuniversity.models;
import com.google.firebase.Timestamp;

public class Complaint {
    private String notePath;
    private String professeurId;
    private String message;
    private Timestamp timestamp;

    // path of the complaint document to allow updates
    private String documentPath;
    // status of the complaint: pending/accepted/rejected
    private String status;
    // optional response message from teacher
    private String response;

    public Complaint() {}

    public String getNotePath() { return notePath; }
    public void setNotePath(String notePath) { this.notePath = notePath; }
    public String getProfesseurId() { return professeurId; }
    public void setProfesseurId(String professeurId) { this.professeurId = professeurId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}