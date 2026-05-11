package model;

public class Doctor {
    public int id;
    public String fullName;
    public String specialization;

    public Doctor(int id, String fullName, String specialization) {
        this.id = id;
        this.fullName = fullName;
        this.specialization = specialization;
    }
}