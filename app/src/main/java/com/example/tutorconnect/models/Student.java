package com.example.tutorconnect.models;

public class Student {
    private String name;
    private String email;
    private int age;
    private String program;
    private String favoriteSubjects;
    private String programmingSkills;

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getFavoriteSubjects() {
        return favoriteSubjects;
    }

    public void setFavoriteSubjects(String favoriteSubjects) {
        this.favoriteSubjects = favoriteSubjects;
    }

    public String getProgrammingSkills() {
        return programmingSkills;
    }

    public void setProgrammingSkills(String programmingSkills) {
        this.programmingSkills = programmingSkills;
    }
}
