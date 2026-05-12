package model;

public class StudyResult extends MedicalRecord {
    private Integer referralId;
    private String studyType;
    private String result;
    private String resultDate;

    public StudyResult(int id, int patientId, int doctorId, String recordDate,
                       Integer referralId, String studyType, String result, String resultDate) {
        super(id, patientId, doctorId, recordDate);
        this.referralId = referralId;
        this.studyType = studyType;
        this.result = result;
        this.resultDate = resultDate;
    }

    public Integer getReferralId() { return referralId; }
    public String getStudyType() { return studyType; }
    public String getResult() { return result; }
    public String getResultDate() { return resultDate; }

    public void setReferralId(Integer referralId) { this.referralId = referralId; }
    public void setStudyType(String studyType) { this.studyType = studyType; }
    public void setResult(String result) { this.result = result; }
    public void setResultDate(String resultDate) { this.resultDate = resultDate; }

    @Override
    public String getDisplayText() {
        return studyType + " (" + resultDate + ")";
    }

    @Override
    public String getDetails() {
        String details = String.format(
                "РЕЗУЛЬТАТ ИССЛЕДОВАНИЯ\n\nДата назначения: %s\nВид: %s\nДата выполнения: %s\nРезультат: %s",
                recordDate, studyType, resultDate, result
        );
        if (referralId != null) {
            details += "\nНаправление от протокола ID: " + referralId;
        }
        return details;
    }
}