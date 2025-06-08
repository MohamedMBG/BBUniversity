package com.example.bbuniversity.models;

import java.util.List;
import java.util.Map;

public class Professeur extends User {

    private String departement;
    private String adresse;

    // clé = nom de la matière, valeur = liste des classes concernées
    private Map<String, List<String>> enseignement;

    public Professeur() {}

    public Professeur(String uid, String nom, String prenom, String email, String role,
                      String departement, String adresse,
                      Map<String, List<String>> enseignement) {
        super(uid, nom, prenom, email, role);
        this.departement = departement;
        this.adresse = adresse;
        this.enseignement = enseignement;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Map<String, List<String>> getEnseignement() {
        return enseignement;
    }

    public void setEnseignement(Map<String, List<String>> enseignement) {
        this.enseignement = enseignement;
    }
}
