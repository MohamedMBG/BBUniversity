package com.example.bbuniversity.models;

import java.util.Date;

public class Abscence {
    private String documentId;
    private String matiere;
    private Date date;
    private boolean justifiee;

    public Abscence() {}

    public Abscence(String matiere, Date date, boolean justifiee) {
        this.matiere = matiere;
        this.date = date;
        this.justifiee = justifiee;
    }

    // Getters & Setters

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isJustifiee() {
        return justifiee;
    }

    public void setJustifiee(boolean justifiee) {
        this.justifiee = justifiee;
    }
}