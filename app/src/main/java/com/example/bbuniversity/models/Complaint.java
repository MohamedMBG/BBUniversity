package com.example.bbuniversity.models;
import com.google.firebase.Timestamp;

public class Complaint {
    private String notePath;
    private String professeurId;
    private String message;
    private Timestamp timestamp;

    public Complaint() {}

    public String getNotePath() { return notePath; }
    public void setNotePath(String notePath) { this.notePath = notePath; }
    public String getProfesseurId() { return professeurId; }
    public void setProfesseurId(String professeurId) { this.professeurId = professeurId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}