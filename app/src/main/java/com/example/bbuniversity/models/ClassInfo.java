package com.example.bbuniversity.models;


public class ClassInfo {
    private String className;
    private String subject;

    public ClassInfo() {}

    public ClassInfo(String className, String subject) {
        this.className = className;
        this.subject = subject;
    }

    public String getClassName() {
        return className;
    }

    public String getSubject() {
        return subject;
    }
}