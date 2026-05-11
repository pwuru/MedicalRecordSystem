package model;

public class Patient {
    public int id;
    public String fullName;
    public String birthDate;
    public String phone;
    public String address;
    public String snils;

    public Patient(int id, String fullName, String birthDate,
                   String phone, String address, String snils) {
        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.snils = snils;
    }
}