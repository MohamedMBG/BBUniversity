package com.example.bbuniversity.models;

public class Etudiant extends User {
    private String matricule;
    private int niveau;
    private String filiere;
    private String classeCode;
    private String classe;

    public Etudiant() {}

    public Etudiant(String uid, String nom, String prenom, String email,
                    String matricule, int niveau, String filiere, String classeCode) {
        super(uid, nom, prenom, email, "student");
        this.matricule = matricule;
        this.niveau = niveau;
        this.filiere = filiere;
        this.classeCode = classeCode;
    }

    // Getters / Setters

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public int getNiveau() {
        return niveau;
    }

    public void setNiveau(int niveau) {
        this.niveau = niveau;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public String getClasseCode() {
        return classeCode;
    }

    public void setClasseCode(String classeCode) {
        this.classeCode = classeCode;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }
}
