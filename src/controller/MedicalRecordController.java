package controller;

import view.MainWindow;
import view.MedicalRecordDialog;
import model.MedicalDB;
import model.MedicalRecord;
import model.Doctor;
import util.LoggerUtil;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

public class MedicalRecordController {
    private MainWindow view;

    public MedicalRecordController(MainWindow view) {
        this.view = view;
    }

    public void loadRecordsForPatient(int patientId) {
        if (patientId == -1) {
            view.getRecordsModel().setRowCount(0);
            return;
        }

        try {
            List<MedicalRecord> records = MedicalDB.getRecordsByPatient(patientId);
            DefaultTableModel model = view.getRecordsModel();
            model.setRowCount(0);

            for (MedicalRecord r : records) {
                String doctorName = MedicalDB.getDoctorName(r.doctorId);

                model.addRow(new Object[]{
                        r.id, r.recordDate, doctorName, r.diagnosis,
                        r.complaints, r.treatment, r.nextAppointment
                });
            }
            view.updateStatus("Записей в истории: " + records.size());
        } catch (SQLException e) {
            view.showError("Ошибка загрузки истории болезни: " + e.getMessage());
        }
    }

    public void addRecord(int patientId) {
        if (patientId == -1) {
            view.showError("Сначала выберите пациента из списка");
            return;
        }

        try {
            List<Doctor> doctors = MedicalDB.getDoctors();
            if (doctors.isEmpty()) {
                view.showError("Нет зарегистрированных врачей. Сначала добавьте врача.");
                return;
            }
        } catch (SQLException e) {
            view.showError("Ошибка загрузки списка врачей: " + e.getMessage());
            return;
        }

        MedicalRecordDialog dialog = new MedicalRecordDialog(view, patientId, null);
        dialog.setVisible(true);
        Object[] data = dialog.getResult();

        if (data != null) {
            try {
                int doctorId = (int) data[0];
                String recordDate = (String) data[1];
                String complaints = (String) data[2];
                String diagnosis = (String) data[3];
                String treatment = (String) data[4];
                String nextAppointment = (String) data[5];

                MedicalDB.addMedicalRecord(patientId, doctorId, recordDate,
                        complaints, diagnosis, treatment, nextAppointment);
                loadRecordsForPatient(patientId);

                String patientName = MedicalDB.getPatientName(patientId);
                String doctorName = MedicalDB.getDoctorName(doctorId);
                String details = String.format(
                        "Врач: %s\nДата приема: %s\nДиагноз: %s\nЖалобы: %s\nЛечение: %s\nСледующий прием: %s",
                        doctorName, recordDate, diagnosis, complaints, treatment,
                        (nextAppointment.isEmpty() ? "не назначен" : nextAppointment)
                );
                LoggerUtil.log(patientId, patientName, "ДОБАВЛЕНИЕ ЗАПИСИ", details);

                view.showSuccess("Запись в историю болезни добавлена");
            } catch (SQLException e) {
                view.showError("Ошибка добавления записи: " + e.getMessage());
            }
        }
    }

    public void editRecord(int patientId) {
        if (patientId == -1) {
            view.showError("Сначала выберите пациента из списка");
            return;
        }

        int row = view.getRecordsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите запись для редактирования");
            return;
        }

        int recordId = (int) view.getRecordsTable().getValueAt(row, 0);

        try {
            MedicalRecord oldRecord = MedicalDB.getRecordById(recordId);
            if (oldRecord == null) {
                view.showError("Запись не найдена");
                return;
            }

            MedicalRecordDialog dialog = new MedicalRecordDialog(view, patientId, oldRecord);
            dialog.setVisible(true);
            Object[] data = dialog.getResult();

            if (data != null) {
                int doctorId = (int) data[0];
                String recordDate = (String) data[1];
                String complaints = (String) data[2];
                String diagnosis = (String) data[3];
                String treatment = (String) data[4];
                String nextAppointment = (String) data[5];

                MedicalDB.updateMedicalRecord(recordId, patientId, doctorId, recordDate,
                        complaints, diagnosis, treatment, nextAppointment);
                loadRecordsForPatient(patientId);

                String patientName = MedicalDB.getPatientName(patientId);
                String doctorName = MedicalDB.getDoctorName(doctorId);
                String oldDoctorName = MedicalDB.getDoctorName(oldRecord.doctorId);
                String details = String.format(
                        "БЫЛО:\n  Врач: %s\n  Дата: %s\n  Диагноз: %s\n  Жалобы: %s\n  Лечение: %s\n  След. прием: %s\n\nСТАЛО:\n  Врач: %s\n  Дата: %s\n  Диагноз: %s\n  Жалобы: %s\n  Лечение: %s\n  След. прием: %s",
                        oldDoctorName, oldRecord.recordDate, oldRecord.diagnosis, oldRecord.complaints, oldRecord.treatment,
                        (oldRecord.nextAppointment == null || oldRecord.nextAppointment.isEmpty() ? "не назначен" : oldRecord.nextAppointment),
                        doctorName, recordDate, diagnosis, complaints, treatment,
                        (nextAppointment.isEmpty() ? "не назначен" : nextAppointment)
                );
                LoggerUtil.log(patientId, patientName, "РЕДАКТИРОВАНИЕ ЗАПИСИ", details);

                view.showSuccess("Запись обновлена");
            }
        } catch (SQLException e) {
            view.showError("Ошибка редактирования: " + e.getMessage());
        }
    }

    public void deleteRecord(int patientId) {
        if (patientId == -1) {
            view.showError("Сначала выберите пациента из списка");
            return;
        }

        int row = view.getRecordsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите запись для удаления");
            return;
        }

        int recordId = (int) view.getRecordsTable().getValueAt(row, 0);
        String diagnosis = (String) view.getRecordsTable().getValueAt(row, 3);

        int confirm = javax.swing.JOptionPane.showConfirmDialog(view,
                "Удалить запись о диагнозе \"" + diagnosis + "\"?",
                "Подтверждение удаления", javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                MedicalRecord record = MedicalDB.getRecordById(recordId);
                String patientName = MedicalDB.getPatientName(patientId);
                String doctorName = MedicalDB.getDoctorName(record.doctorId);

                MedicalDB.deleteMedicalRecord(recordId);
                loadRecordsForPatient(patientId);

                String details = String.format(
                        "Удалена запись:\n  Врач: %s\n  Дата: %s\n  Диагноз: %s\n  Жалобы: %s\n  Лечение: %s\n  След. прием: %s",
                        doctorName, record.recordDate, record.diagnosis, record.complaints, record.treatment,
                        (record.nextAppointment == null || record.nextAppointment.isEmpty() ? "не назначен" : record.nextAppointment)
                );
                LoggerUtil.log(patientId, patientName, "УДАЛЕНИЕ ЗАПИСИ", details);

                view.showSuccess("Запись удалена");
            } catch (SQLException e) {
                view.showError("Ошибка удаления: " + e.getMessage());
            }
        }
    }
}