package view;

import model.MedicalDB;
import model.Doctor;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MedicalRecordDialog extends JDialog {
    private JComboBox<String> doctorComboBox;
    private JTextField recordDateField, nextAppointmentField;
    private JTextArea complaintsArea, diagnosisArea, treatmentArea;
    private boolean confirmed = false;

    public MedicalRecordDialog(JFrame parent, int patientId, model.MedicalRecord existingRecord) {
        super(parent, existingRecord == null ? "Добавить запись" : "Редактировать запись", true);
        initComponents(existingRecord);
        setSize(550, 550);
        setLocationRelativeTo(parent);
    }

    private void initComponents(model.MedicalRecord existingRecord) {
        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel("Врач:"));
        doctorComboBox = new JComboBox<>();
        loadDoctors(existingRecord);
        fieldsPanel.add(doctorComboBox);

        fieldsPanel.add(new JLabel("Дата приема (ГГГГ-ММ-ДД):"));
        String defaultDate = java.time.LocalDate.now().toString();
        String existingDate = (existingRecord != null && existingRecord.recordDate != null)
                ? existingRecord.recordDate : defaultDate;
        recordDateField = new JTextField(existingDate);
        fieldsPanel.add(recordDateField);

        fieldsPanel.add(new JLabel("Жалобы:"));
        complaintsArea = new JTextArea(3, 20);
        if (existingRecord != null && existingRecord.complaints != null) {
            complaintsArea.setText(existingRecord.complaints);
        }
        JScrollPane complaintsScroll = new JScrollPane(complaintsArea);
        fieldsPanel.add(complaintsScroll);

        fieldsPanel.add(new JLabel("Диагноз:"));
        diagnosisArea = new JTextArea(3, 20);
        if (existingRecord != null && existingRecord.diagnosis != null) {
            diagnosisArea.setText(existingRecord.diagnosis);
        }
        JScrollPane diagnosisScroll = new JScrollPane(diagnosisArea);
        fieldsPanel.add(diagnosisScroll);

        fieldsPanel.add(new JLabel("Назначенное лечение:"));
        treatmentArea = new JTextArea(3, 20);
        if (existingRecord != null && existingRecord.treatment != null) {
            treatmentArea.setText(existingRecord.treatment);
        }
        JScrollPane treatmentScroll = new JScrollPane(treatmentArea);
        fieldsPanel.add(treatmentScroll);

        fieldsPanel.add(new JLabel("Дата след. приема:"));
        String existingNext = (existingRecord != null && existingRecord.nextAppointment != null)
                ? existingRecord.nextAppointment : "";
        nextAppointmentField = new JTextField(existingNext);
        fieldsPanel.add(nextAppointmentField);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Отмена");

        okBtn.addActionListener(e -> {
            if (validateFields()) {
                confirmed = true;
                dispose();
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        buttonsPanel.add(okBtn);
        buttonsPanel.add(cancelBtn);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void loadDoctors(model.MedicalRecord existingRecord) {
        try {
            List<Doctor> doctors = MedicalDB.getDoctors();
            int selectedIndex = 0;

            for (int i = 0; i < doctors.size(); i++) {
                Doctor d = doctors.get(i);
                doctorComboBox.addItem(d.id + " - " + d.fullName + " (" + d.specialization + ")");

                if (existingRecord != null && existingRecord.doctorId == d.id) {
                    selectedIndex = i;
                }
            }

            doctorComboBox.setSelectedIndex(selectedIndex);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки списка врачей: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateFields() {
        if (doctorComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Выберите врача", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (recordDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите дату приема", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String recordDate = recordDateField.getText().trim();
        if (!recordDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Дата должна быть в формате ГГГГ-ММ-ДД (например, 2025-05-10)",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (diagnosisArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите диагноз", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String nextAppointment = nextAppointmentField.getText().trim();
        if (!nextAppointment.isEmpty() && !nextAppointment.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Дата следующего приема должна быть в формате ГГГГ-ММ-ДД",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public Object[] getResult() {
        if (!confirmed) return null;

        int selectedIndex = doctorComboBox.getSelectedIndex();
        String selectedItem = doctorComboBox.getItemAt(selectedIndex);
        int doctorId = Integer.parseInt(selectedItem.split(" - ")[0]);

        return new Object[]{
                doctorId,
                recordDateField.getText().trim(),
                complaintsArea.getText().trim(),
                diagnosisArea.getText().trim(),
                treatmentArea.getText().trim(),
                nextAppointmentField.getText().trim()
        };
    }
}