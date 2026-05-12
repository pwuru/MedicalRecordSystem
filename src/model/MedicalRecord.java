package model;

public abstract class MedicalRecord {
    protected int id;
    protected int patientId;
    protected int doctorId;
    protected String recordDate;

    public MedicalRecord(int id, int patientId, int doctorId, String recordDate) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.recordDate = recordDate;
    }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public String getRecordDate() { return recordDate; }

    public void setId(int id) { this.id = id; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }

    public abstract String getDisplayText();
    public abstract String getDetails();
}