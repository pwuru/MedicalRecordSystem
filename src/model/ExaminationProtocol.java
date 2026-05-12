package model;

public class ExaminationProtocol extends MedicalRecord {
    private String complaints;
    private String diagnosis;
    private String treatment;
    private String nextAppointment;

    public ExaminationProtocol(int id, int patientId, int doctorId, String recordDate,
                               String complaints, String diagnosis, String treatment, String nextAppointment) {
        super(id, patientId, doctorId, recordDate);
        this.complaints = complaints;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.nextAppointment = nextAppointment;
    }

    public String getComplaints() { return complaints; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatment() { return treatment; }
    public String getNextAppointment() { return nextAppointment; }

    public void setComplaints(String complaints) { this.complaints = complaints; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public void setNextAppointment(String nextAppointment) { this.nextAppointment = nextAppointment; }

    @Override
    public String getDisplayText() {
        return diagnosis + " (" + recordDate + ")";
    }

    @Override
    public String getDetails() {
        return String.format(
                "ПРОТОКОЛ ОСМОТРА\n\nДата приема: %s\nЖалобы: %s\nДиагноз: %s\nЛечение: %s\nСлед. прием: %s",
                recordDate,
                complaints != null ? complaints : "не указаны",
                diagnosis,
                treatment != null ? treatment : "не указано",
                nextAppointment != null && !nextAppointment.isEmpty() ? nextAppointment : "не назначен"
        );
    }
}