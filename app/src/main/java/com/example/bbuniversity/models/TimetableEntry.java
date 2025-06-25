package com.example.bbuniversity.models;

/**
 * Représente une séance dans l'emploi du temps d'une classe
 */
public class TimetableEntry {
    /** Jour de la semaine (ex: Monday) */
    private String day;
    /** Nom de la matière */
    private String subject;
    /** Heure de début au format HH:mm */
    private String start;
    /** Heure de fin au format HH:mm */
    private String end;

    public TimetableEntry(){}

    public TimetableEntry(String day, String subject, String start, String end){
        this.day = day;
        this.subject = subject;
        this.start = start;
        this.end = end;
    }

    public String getDay(){ return day; }
    public String getSubject(){ return subject; }
    public String getStart(){ return start; }
    public String getEnd(){ return end; }

    public void setDay(String day){ this.day = day; }
    public void setSubject(String subject){ this.subject = subject; }
    public void setStart(String start){ this.start = start; }
    public void setEnd(String end){ this.end = end; }
}