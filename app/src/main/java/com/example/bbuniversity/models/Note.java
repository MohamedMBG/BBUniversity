package com.example.bbuniversity.models;

import com.google.firebase.Timestamp;

public class Note {
    private String matiere;  // Changed from matiereId
    private double participation;  // Changed from noteParticipation
    private double controle;  // Changed from noteCC
    private double examenFinal;  // Changed from noteExamen
    private double noteGenerale;  // Changed from noteTotale
    private Timestamp derniereMiseAJour;
    private String professeurId;
    private String documentPath;

    public Note() {
    }

    // Getters and setters for all fields
    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public double getParticipation() {
        return participation;
    }

    public void setParticipation(double participation) {
        this.participation = participation;
    }

    public double getControle() {
        return controle;
    }

    public void setControle(double controle) {
        this.controle = controle;
    }

    public double getExamenFinal() {
        return examenFinal;
    }

    public void setExamenFinal(double examenFinal) {
        this.examenFinal = examenFinal;
    }

    public double getNoteGenerale() {
        return noteGenerale;
    }

    public void setNoteGenerale(double noteGenerale) {
        this.noteGenerale = noteGenerale;
    }

    public Timestamp getDerniereMiseAJour() {
        return derniereMiseAJour;
    }

    public void setDerniereMiseAJour(Timestamp derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }

    public String getProfesseurId() {
        return professeurId;
    }

    public void setProfesseurId(String professeurId) {
        this.professeurId = professeurId;
    }

    // Add getter & setter
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String path) { this.documentPath = path; }
}