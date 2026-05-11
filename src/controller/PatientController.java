package controller;

import view.MainWindow;
import view.PatientDialog;
import model.MedicalDB;
import model.Patient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientController {
    private MainWindow view;

    public PatientController(MainWindow view) {
        this.view = view;
        loadPatients();
    }

    public void loadPatients() {
        try {
            List<Patient> patients;
            if (view.getCurrentUser().getRole().equals("PATIENT")) {
                Integer patientId = view.getCurrentUser().getPatientId();
                patients = new ArrayList<>();
                if (patientId != null) {
                    Patient p = MedicalDB.getPatientById(patientId);
                    if (p != null) {
                        patients.add(p);
                    }
                }
            } else {
                patients = MedicalDB.getPatients();
            }

            DefaultTableModel model = view.getPatientsModel();
            model.setRowCount(0);
            for (Patient p : patients) {
                model.addRow(new Object[]{
                        p.id, p.fullName, p.birthDate, p.phone, p.address, p.snils
                });
            }
            view.updateStatus("Загружено пациентов: " + patients.size());
        } catch (SQLException e) {
            view.showError("Ошибка загрузки пациентов: " + e.getMessage());
        }
        refreshPatientComboBox();
    }

    public void refreshPatientComboBox() {
        try {
            List<Patient> patients;
            int selectedId = view.getCurrentPatientId();

            if (view.getCurrentUser().getRole().equals("PATIENT")) {
                Integer patientId = view.getCurrentUser().getPatientId();
                if (patientId != null) {
                    patients = new ArrayList<>();
                    Patient p = MedicalDB.getPatientById(patientId);
                    if (p != null) {
                        patients.add(p);
                    }
                    selectedId = patientId;
                } else {
                    patients = MedicalDB.getPatients();
                }
            } else {
                patients = MedicalDB.getPatients();
            }

            view.updatePatientComboBox(patients, selectedId);

            if (patients.isEmpty()) {
                view.clearRecordsTable();
                view.updateStatus("Нет зарегистрированных пациентов");
            } else {
                MedicalRecordController recordController = view.getRecordController();

                if (selectedId == -1 && !patients.isEmpty()) {
                    selectedId = patients.get(0).id;
                    view.setCurrentPatientId(selectedId);

                    for (int i = 0; i < view.getPatientsComboBox().getItemCount(); i++) {
                        String item = view.getPatientsComboBox().getItemAt(i);
                        if (item.startsWith(selectedId + " -")) {
                            view.getPatientsComboBox().setSelectedIndex(i);
                            break;
                        }
                    }
                }

                if (recordController != null && selectedId != -1) {
                    recordController.loadRecordsForPatient(selectedId, view.getCurrentRecordType());
                }

                String patientName = MedicalDB.getPatientName(selectedId);
                view.updateStatus("Выбран пациент: " + patientName);
            }
        } catch (SQLException e) {
            view.showError("Ошибка загрузки пациентов: " + e.getMessage());
        }
    }

    public String getPatientName(int patientId) {
        try {
            return MedicalDB.getPatientName(patientId);
        } catch (SQLException e) {
            view.showError("Ошибка получения имени пациента: " + e.getMessage());
            return "Неизвестный пациент";
        }
    }

    public void addPatient() {
        PatientDialog dialog = new PatientDialog(view, "", "", "", "", "");
        dialog.setVisible(true);
        String[] data = dialog.getResult();
        if (data != null) {
            try {
                MedicalDB.addPatient(data[0], data[1], data[2], data[3], data[4]);
                loadPatients();
                view.showSuccess("Пациент '" + data[0] + "' успешно добавлен");
            } catch (SQLException e) {
                if (e.getMessage().contains("UNIQUE")) {
                    view.showError("Пациент с таким СНИЛС уже существует");
                } else {
                    view.showError("Ошибка: " + e.getMessage());
                }
            }
        }
    }

    public void editPatient() {
        int row = view.getPatientsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите пациента для редактирования");
            return;
        }

        int id = (int) view.getPatientsTable().getValueAt(row, 0);
        String name = (String) view.getPatientsTable().getValueAt(row, 1);
        String birthDate = (String) view.getPatientsTable().getValueAt(row, 2);
        String phone = (String) view.getPatientsTable().getValueAt(row, 3);
        String address = (String) view.getPatientsTable().getValueAt(row, 4);
        String snils = (String) view.getPatientsTable().getValueAt(row, 5);

        PatientDialog dialog = new PatientDialog(view, name, birthDate, phone, address, snils);
        dialog.setVisible(true);
        String[] data = dialog.getResult();
        if (data != null) {
            try {
                MedicalDB.updatePatient(id, data[0], data[1], data[2], data[3], data[4]);
                loadPatients();
                view.showSuccess("Данные пациента обновлены");
            } catch (SQLException e) {
                view.showError("Ошибка: " + e.getMessage());
            }
        }
    }

    public void deletePatient() {
        int row = view.getPatientsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите пациента для удаления");
            return;
        }

        String patientName = (String) view.getPatientsTable().getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(view,
                "Удалить пациента \"" + patientName + "\" и всю его историю болезни?\nЭто действие необратимо.",
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) view.getPatientsTable().getValueAt(row, 0);
            try {
                MedicalDB.deletePatient(id);
                loadPatients();

                view.getRecordsModel().setRowCount(0);
                view.updateStatus("Пациент удален");
                view.showSuccess("Пациент успешно удален");
            } catch (SQLException e) {
                view.showError("Ошибка удаления: " + e.getMessage());
            }
        }
    }
}