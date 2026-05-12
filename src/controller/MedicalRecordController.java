package controller;

import view.MainWindow;
import view.ExaminationProtocolDialog;
import view.StudyResultDialog;
import model.*;
import util.LoggerUtil;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

public class MedicalRecordController {
    private MainWindow view;

    public MedicalRecordController(MainWindow view) {
        this.view = view;
    }

    public boolean isDatabaseConnected() {
        return MedicalDB.isConnected();
    }

    public void loadRecordsForPatient(int patientId, int recordType) {
        if (patientId == -1) {
            view.getRecordsModel().setRowCount(0);
            return;
        }

        try {
            if (recordType == 0) {
                loadExaminationProtocols(patientId);
            } else {
                loadStudyResults(patientId);
            }
        } catch (SQLException e) {
            view.showError("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void loadExaminationProtocols(int patientId) throws SQLException {
        List<ExaminationProtocol> protocols = MedicalDB.getExaminationProtocolsByPatient(patientId);
        DefaultTableModel model = view.getRecordsModel();
        model.setRowCount(0);

        for (ExaminationProtocol p : protocols) {
            String doctorName = MedicalDB.getDoctorName(p.getDoctorId());
            String complaints = p.getComplaints() != null && p.getComplaints().length() > 30 ?
                    p.getComplaints().substring(0, 27) + "..." : p.getComplaints();
            model.addRow(new Object[]{
                    p.getId(), p.getRecordDate(), doctorName, complaints, p.getDiagnosis()
            });
        }
        view.updateStatus("Протоколов осмотров: " + protocols.size());
    }

    private void loadStudyResults(int patientId) throws SQLException {
        List<StudyResult> studies = MedicalDB.getStudyResultsByPatient(patientId);
        DefaultTableModel model = view.getRecordsModel();
        model.setRowCount(0);

        for (StudyResult s : studies) {
            String doctorName = MedicalDB.getDoctorName(s.getDoctorId());
            String resultShort = s.getResult() != null && s.getResult().length() > 30 ?
                    s.getResult().substring(0, 27) + "..." : s.getResult();
            model.addRow(new Object[]{
                    s.getId(), s.getResultDate(), doctorName, s.getStudyType(), resultShort
            });
        }
        view.updateStatus("Результатов исследований: " + studies.size());
    }

    public void setupTableForProtocols(DefaultTableModel model) {
        model.setColumnCount(0);
        model.setColumnIdentifiers(new String[]{"ID", "Дата", "Врач", "Жалобы", "Диагноз"});
        model.fireTableStructureChanged();
    }

    public void setupTableForStudies(DefaultTableModel model) {
        model.setColumnCount(0);
        model.setColumnIdentifiers(new String[]{"ID", "Дата выполнения", "Врач", "Вид исследования", "Результат"});
        model.fireTableStructureChanged();
    }

    public void addRecord(int patientId, int recordType) {
        if (patientId == -1) {
            view.showError("Сначала выберите пациента");
            return;
        }

        try {
            if (MedicalDB.getDoctors().isEmpty()) {
                view.showError("Нет зарегистрированных врачей");
                return;
            }
        } catch (SQLException e) {
            view.showError("Ошибка: " + e.getMessage());
            return;
        }

        if (recordType == 0) {
            addExaminationProtocol(patientId);
        } else {
            addStudyResult(patientId);
        }
    }

    public void editRecord(int patientId, int recordType) {
        if (patientId == -1) {
            view.showError("Сначала выберите пациента");
            return;
        }

        int row = view.getRecordsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите запись");
            return;
        }

        if (recordType == 0) {
            editExaminationProtocol(patientId, row);
        } else {
            editStudyResult(patientId, row);
        }
    }

    public void deleteRecord(int patientId, int recordType) {
        if (patientId == -1) {
            view.showError("Сначала выберите пациента");
            return;
        }

        int row = view.getRecordsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите запись");
            return;
        }

        if (recordType == 0) {
            deleteExaminationProtocol(patientId, row);
        } else {
            deleteStudyResult(patientId, row);
        }
    }

    private boolean isOwnRecord(int doctorId) {
        User currentUser = view.getCurrentUser();

        if (currentUser.getRole().equals("ADMIN")) {
            return false;
        }

        if (currentUser.getRole().equals("DOCTOR")) {
            try {
                Doctor doctor = MedicalDB.getDoctorByUserId(currentUser.getId());
                if (doctor == null) {
                    return true;
                }
                return doctor.id != doctorId;
            } catch (SQLException e) {
                view.showError("Ошибка проверки прав: " + e.getMessage());
                return true;
            }
        }
        return true;
    }

    private void addExaminationProtocol(int patientId) {
        try {
            User currentUser = view.getCurrentUser();
            List<Doctor> doctors = MedicalDB.getDoctorsWithFilter(currentUser.getId(), currentUser.getRole());
            if (doctors.isEmpty()) {
                view.showError("Нет врачей");
                return;
            }

            ExaminationProtocolDialog dialog = new ExaminationProtocolDialog(view, patientId, null, doctors);
            dialog.setVisible(true);
            Object[] data = dialog.getResult();

            if (data != null) {
                int doctorId = (int) data[0];

                if (isOwnRecord(doctorId)) {
                    view.showError("Вы можете создавать записи только от своего имени");
                    return;
                }

                String recordDate = (String) data[1];
                String complaints = (String) data[2];
                String diagnosis = (String) data[3];
                String treatment = (String) data[4];
                String nextAppointment = (String) data[5];

                MedicalDB.addExaminationProtocol(patientId, doctorId, recordDate,
                        complaints, diagnosis, treatment, nextAppointment);

                loadRecordsForPatient(patientId, 0);

                String patientName = MedicalDB.getPatientName(patientId);
                String doctorName = MedicalDB.getDoctorName(doctorId);
                String treatmentStr = (treatment == null || treatment.isEmpty()) ? "Не назначено" : treatment;
                String nextAppointmentStr = (nextAppointment == null || nextAppointment.isEmpty()) ? "Не назначена" : nextAppointment;
                String details = String.format(
                        "Врач: %s\n  Дата: %s\n  Жалобы: %s\n  Диагноз: %s\n  Лечение: %s\n  След. прием: %s",
                        doctorName, recordDate, complaints, diagnosis, treatmentStr, nextAppointmentStr
                );
                LoggerUtil.log(patientId, patientName, "ДОБАВЛЕНИЕ ПРОТОКОЛА", details);

                view.showSuccess("Протокол осмотра добавлен");
            }
        } catch (SQLException e) {
            view.showError("Ошибка: " + e.getMessage());
        }
    }

    private void editExaminationProtocol(int patientId, int row) {
        int protocolId = (int) view.getRecordsModel().getValueAt(row, 0);
        try {
            ExaminationProtocol oldProtocol = MedicalDB.getExaminationProtocolById(protocolId);
            if (oldProtocol == null) {
                view.showError("Протокол не найден");
                return;
            }

            User currentUser = view.getCurrentUser();

            if (currentUser.getRole().equals("DOCTOR")) {
                Doctor currentDoctor = MedicalDB.getDoctorByUserId(currentUser.getId());
                if (currentDoctor == null || currentDoctor.id != oldProtocol.getDoctorId()) {
                    view.showError("Вы можете редактировать только свои протоколы");
                    return;
                }
            }

            List<Doctor> doctors = MedicalDB.getDoctorsWithFilter(currentUser.getId(), currentUser.getRole());
            ExaminationProtocolDialog dialog = new ExaminationProtocolDialog(view, patientId, oldProtocol, doctors);
            dialog.setVisible(true);
            Object[] data = dialog.getResult();

            if (data != null) {
                int doctorId = (int) data[0];
                String recordDate = (String) data[1];
                String complaints = (String) data[2];
                String diagnosis = (String) data[3];
                String treatment = (String) data[4];
                String nextAppointment = (String) data[5];

                MedicalDB.updateExaminationProtocol(protocolId, patientId, doctorId, recordDate,
                        complaints, diagnosis, treatment, nextAppointment);

                loadRecordsForPatient(patientId, 0);

                String patientName = MedicalDB.getPatientName(patientId);
                String doctorName = MedicalDB.getDoctorName(doctorId);
                String treatmentStr = (treatment == null || treatment.isEmpty()) ? "Не назначено" : treatment;
                String nextAppointmentStr = (nextAppointment == null || nextAppointment.isEmpty()) ? "Не назначена" : nextAppointment;
                String details = String.format(
                        "Врач: %s\n  Дата: %s\n  Жалобы: %s\n  Диагноз: %s\n  Лечение: %s\n  След. прием: %s",
                        doctorName, recordDate, complaints, diagnosis, treatmentStr, nextAppointmentStr
                );
                LoggerUtil.log(patientId, patientName, "РЕДАКТИРОВАНИЕ ПРОТОКОЛА", details);

                view.showSuccess("Протокол обновлен");
            }
        } catch (SQLException e) {
            view.showError("Ошибка: " + e.getMessage());
        }
    }

    private void deleteExaminationProtocol(int patientId, int row) {
        int protocolId = (int) view.getRecordsModel().getValueAt(row, 0);
        try {
            ExaminationProtocol protocol = MedicalDB.getExaminationProtocolById(protocolId);

            if (protocol == null) {
                view.showError("Протокол не найден");
                return;
            }

            if (isOwnRecord(protocol.getDoctorId())) {
                view.showError("Вы можете удалять только свои записи");
                return;
            }

            String patientName = MedicalDB.getPatientName(patientId);
            String doctorName = MedicalDB.getDoctorName(protocol.getDoctorId());

            MedicalDB.deleteExaminationProtocol(protocolId);
            loadRecordsForPatient(patientId, 0);

            String details = String.format(
                    "Врач: %s\n  Дата: %s\n  Диагноз: %s",
                    doctorName, protocol.getRecordDate(), protocol.getDiagnosis()
            );
            LoggerUtil.log(patientId, patientName, "УДАЛЕНИЕ ПРОТОКОЛА", details);

            view.showSuccess("Протокол удален");
        } catch (SQLException e) {
            view.showError("Ошибка: " + e.getMessage());
        }
    }

    private void addStudyResult(int patientId) {
        try {
            User currentUser = view.getCurrentUser();
            List<Doctor> doctors = MedicalDB.getDoctorsWithFilter(currentUser.getId(), currentUser.getRole());
            if (doctors.isEmpty()) {
                view.showError("Нет врачей");
                return;
            }

            List<ExaminationProtocol> protocols = MedicalDB.getExaminationProtocolsByPatient(patientId);

            StudyResultDialog dialog = new StudyResultDialog(view, patientId, null, doctors, protocols);
            dialog.setVisible(true);
            Object[] data = dialog.getResult();

            if (data != null) {
                int doctorId = (int) data[0];

                if (isOwnRecord(doctorId)) {
                    view.showError("Вы можете добавлять результаты исследования только от своего имени");
                    return;
                }

                String recordDate = (String) data[1];
                Integer referralId = (Integer) data[2];
                String studyType = (String) data[3];
                String result = (String) data[4];
                String resultDate = (String) data[5];

                MedicalDB.addStudyResult(patientId, doctorId, recordDate,
                        referralId, studyType, result, resultDate);

                loadRecordsForPatient(patientId, 1);

                String patientName = MedicalDB.getPatientName(patientId);
                String doctorName = MedicalDB.getDoctorName(doctorId);
                String recordDateStr = (recordDate == null || recordDate.isEmpty()) ? "Не указана" : resultDate;
                String details = String.format(
                        "Врач: %s\n  Дата назначения: %s\n  Вид: %s\n  Результат: %s\n  Дата выполнения: %s",
                        doctorName, recordDateStr, studyType, result, resultDate
                );
                if (referralId != null) {
                    details += "\n  Направление от протокола ID: " + referralId;
                }
                LoggerUtil.log(patientId, patientName, "ДОБАВЛЕНИЕ РЕЗУЛЬТАТА", details);

                view.showSuccess("Результат исследования добавлен");
            }
        } catch (SQLException e) {
            view.showError("Ошибка: " + e.getMessage());
        }
    }

    private void editStudyResult(int patientId, int row) {
        int studyId = (int) view.getRecordsModel().getValueAt(row, 0);
        try {
            StudyResult oldStudy = MedicalDB.getStudyResultById(studyId);
            if (oldStudy == null) {
                view.showError("Запись не найдена");
                return;
            }
            User currentUser = view.getCurrentUser();

            if (currentUser.getRole().equals("DOCTOR")) {
                Doctor currentDoctor = MedicalDB.getDoctorByUserId(currentUser.getId());
                if (currentDoctor == null || currentDoctor.id != oldStudy.getDoctorId()) {
                    view.showError("Вы можете редактировать только свои результаты исследований");
                    return;
                }
            }

            List<Doctor> doctors = MedicalDB.getDoctorsWithFilter(currentUser.getId(), currentUser.getRole());
            List<ExaminationProtocol> protocols = MedicalDB.getExaminationProtocolsByPatient(patientId);

            StudyResultDialog dialog = new StudyResultDialog(view, patientId, oldStudy, doctors, protocols);
            dialog.setVisible(true);
            Object[] data = dialog.getResult();

            if (data != null) {
                int doctorId = (int) data[0];

                String recordDate = (String) data[1];
                Integer referralId = (Integer) data[2];
                String studyType = (String) data[3];
                String result = (String) data[4];
                String resultDate = (String) data[5];

                MedicalDB.updateStudyResult(studyId, patientId, doctorId, recordDate,
                        referralId, studyType, result, resultDate);

                loadRecordsForPatient(patientId, 1);

                String patientName = MedicalDB.getPatientName(patientId);
                String doctorName = MedicalDB.getDoctorName(doctorId);
                String recordDateDtr = (recordDate == null || recordDate.isEmpty()) ? "Не указана" : recordDate;
                String details = String.format(
                        "Врач: %s\n  Дата назначения: %s\n  Вид: %s\n  Результат: %s\n  Дата выполнения: %s",
                        doctorName, recordDateDtr, studyType, result, resultDate
                );
                if (referralId != null) {
                    details += "\n  Направление от протокола ID: " + referralId;
                }
                LoggerUtil.log(patientId, patientName, "РЕДАКТИРОВАНИЕ РЕЗУЛЬТАТА", details);

                view.showSuccess("Результат исследования обновлен");
            }
        } catch (SQLException e) {
            view.showError("Ошибка: " + e.getMessage());
        }
    }

    private void deleteStudyResult(int patientId, int row) {
        int studyId = (int) view.getRecordsModel().getValueAt(row, 0);
        try {
            StudyResult study = MedicalDB.getStudyResultById(studyId);

            if (study == null) {
                view.showError("Исследование не найдено");
                return;
            }

            if (isOwnRecord(study.getDoctorId())) {
                view.showError("Вы можете удалять только свои результаты исследований");
                return;
            }

            String patientName = MedicalDB.getPatientName(patientId);
            String doctorName = MedicalDB.getDoctorName(study.getDoctorId());

            MedicalDB.deleteStudyResult(studyId);
            loadRecordsForPatient(patientId, 1);

            String details = String.format(
                    "Врач: %s\n  Вид: %s\n  Результат: %s",
                    doctorName, study.getStudyType(), study.getResult()
            );
            LoggerUtil.log(patientId, patientName, "УДАЛЕНИЕ РЕЗУЛЬТАТА", details);

            view.showSuccess("Результат исследования удален");
        } catch (SQLException e) {
            view.showError("Ошибка: " + e.getMessage());
        }
    }
}