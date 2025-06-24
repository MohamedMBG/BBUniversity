package com.example.bbuniversity.models;
import com.google.firebase.Timestamp;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Complaint {
    private String studentId;        // UID of the student who filed
    private String teacherId;        // UID of the teacher handling it
    private String subjectId;        // e.g. "math_101" or course code
    private String noteId;           // document path or ID of the grade entry
    private double initialGrade;        // the original grade
    private double modifiedGrade;       // grade after teacher’s decision
    private String title;            // objet / subject of the complaint
    private String description;      // detailed message from the student
    private String response;         // teacher’s response text
    private String status;           // e.g. "pending", "accepted", "rejected"
    private Timestamp dateFiled;     // when the complaint was submitted
    private Timestamp dateProcessed; // when teacher accepted/rejected
    @Exclude
    private String documentPath;

    // Mandatory public no-arg constructor for Firestore
    public Complaint() {}

    public Complaint(String studentId,
                     String teacherId,
                     String subjectId,
                     String noteId,
                     double initialGrade,
                     double modifiedGrade,
                     String title,
                     String description,
                     String response,
                     String status,
                     Timestamp dateFiled,
                     Timestamp dateProcessed) {
        this.studentId       = studentId;
        this.teacherId       = teacherId;
        this.subjectId       = subjectId;
        this.noteId          = noteId;
        this.initialGrade    = initialGrade;
        this.modifiedGrade   = modifiedGrade;
        this.title           = title;
        this.description     = description;
        this.response        = response;
        this.status          = status;
        this.dateFiled       = dateFiled;
        this.dateProcessed   = dateProcessed;

    }

    // ────────────── Getters & Setters ──────────────

    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTeacherId() {
        return teacherId;
    }
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getNoteId() {
        return noteId;
    }
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public double getInitialGrade() {
        return initialGrade;
    }
    public void setInitialGrade(int initialGrade) {
        this.initialGrade = initialGrade;
    }

    public double getModifiedGrade() {
        return modifiedGrade;
    }
    public void setModifiedGrade(int modifiedGrade) {
        this.modifiedGrade = modifiedGrade;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getDateFiled() {
        return dateFiled;
    }
    public void setDateFiled(Timestamp dateFiled) {
        this.dateFiled = dateFiled;
    }

    public Timestamp getDateProcessed() {
        return dateProcessed;
    }
    public void setDateProcessed(Timestamp dateProcessed) {
        this.dateProcessed = dateProcessed;
    }
    @Exclude
    public String getDocumentPath() {
        return documentPath;
    }

    @Exclude
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }
}
