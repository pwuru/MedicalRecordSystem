package controller;

import view.MainWindow;
import view.DoctorDialog;
import model.MedicalDB;
import model.Doctor;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

public class DoctorController {
    private MainWindow view;

    public DoctorController(MainWindow view) {
        this.view = view;
        loadDoctors();
    }

    public void loadDoctors() {
        try {
            List<Doctor> doctors = MedicalDB.getDoctors();
            DefaultTableModel model = view.getDoctorsModel();
            model.setRowCount(0);
            for (Doctor d : doctors) {
                model.addRow(new Object[]{
                        d.id, d.fullName, d.specialization
                });
            }
            view.updateStatus("Загружено врачей: " + doctors.size());
        } catch (SQLException e) {
            view.showError("Ошибка загрузки врачей: " + e.getMessage());
        }
    }

    public void addDoctor() {
        DoctorDialog dialog = new DoctorDialog(view, "", "");
        dialog.setVisible(true);
        String[] data = dialog.getResult();
        if (data != null) {
            try {
                MedicalDB.addDoctor(data[0], data[1]);
                loadDoctors();
                view.showSuccess("Врач '" + data[0] + "' успешно добавлен");
            } catch (SQLException e) {
                view.showError("Ошибка: " + e.getMessage());
            }
        }
    }

    public void editDoctor() {
        int row = view.getDoctorsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите врача для редактирования");
            return;
        }

        int id = (int) view.getDoctorsTable().getValueAt(row, 0);
        String name = (String) view.getDoctorsTable().getValueAt(row, 1);
        String specialization = (String) view.getDoctorsTable().getValueAt(row, 2);

        DoctorDialog dialog = new DoctorDialog(view, name, specialization);
        dialog.setVisible(true);
        String[] data = dialog.getResult();
        if (data != null) {
            try {
                MedicalDB.updateDoctor(id, data[0], data[1]);
                loadDoctors();
                view.showSuccess("Данные врача обновлены");
            } catch (SQLException e) {
                view.showError("Ошибка: " + e.getMessage());
            }
        }
    }

    public void deleteDoctor() {
        int row = view.getDoctorsTable().getSelectedRow();
        if (row == -1) {
            view.showError("Сначала выберите врача для удаления");
            return;
        }

        String doctorName = (String) view.getDoctorsTable().getValueAt(row, 1);

        try {
            int id = (int) view.getDoctorsTable().getValueAt(row, 0);
            if (MedicalDB.hasDoctorRecords(id)) {
                view.showError("Нельзя удалить врача, так как у него есть записи в истории болезни");
                return;
            }
        } catch (SQLException e) {
            view.showError("Ошибка проверки: " + e.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Удалить врача \"" + doctorName + "\"?",
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) view.getDoctorsTable().getValueAt(row, 0);
            try {
                MedicalDB.deleteDoctor(id);
                loadDoctors();
                view.updateStatus("Врач удален");
                view.showSuccess("Врач успешно удален");
            } catch (SQLException e) {
                view.showError("Ошибка удаления: " + e.getMessage());
            }
        }
    }
}
