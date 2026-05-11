package model;

public class MedicalRecord {
    public int id;
    public int patientId;
    public int doctorId;
    public String recordDate;
    public String complaints;
    public String diagnosis;
    public String treatment;
    public String nextAppointment;

    public MedicalRecord(int id, int patientId, int doctorId,
                         String recordDate, String complaints,
                         String diagnosis, String treatment,
                         String nextAppointment) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.recordDate = recordDate;
        this.complaints = complaints;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.nextAppointment = nextAppointment;
    }
}