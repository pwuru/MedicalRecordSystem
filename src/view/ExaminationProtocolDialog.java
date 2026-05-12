package view;

import model.Doctor;
import model.ExaminationProtocol;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ExaminationProtocolDialog extends JDialog {
    private JComboBox<String> doctorComboBox;
    private JTextField recordDateField, nextAppointmentField;
    private JTextArea complaintsArea, diagnosisArea, treatmentArea;
    private boolean confirmed = false;

    public ExaminationProtocolDialog(JFrame parent, int patientId,
                                     ExaminationProtocol existingRecord,
                                     List<Doctor> doctors) {  // ← передаём список врачей
        super(parent, existingRecord == null ? "Добавить протокол осмотра" : "Редактировать протокол осмотра", true);
        initComponents(existingRecord, doctors);
        setSize(600, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents(ExaminationProtocol existingRecord, List<Doctor> doctors) {
        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel("*Врач:"));
        doctorComboBox = new JComboBox<>();

        int selectedIndex = 0;
        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            doctorComboBox.addItem(d.id + " - " + d.fullName + " (" + d.specialization + ")");
            if (existingRecord != null && existingRecord.getDoctorId() == d.id) {
                selectedIndex = i;
            }
        }
        doctorComboBox.setSelectedIndex(selectedIndex);
        fieldsPanel.add(doctorComboBox);

        fieldsPanel.add(new JLabel("*Дата приема (ГГГГ-ММ-ДД):"));
        String existingDate;
        if (existingRecord != null && existingRecord.getRecordDate() != null) {
            existingDate = existingRecord.getRecordDate();
        } else {
            existingDate = java.time.LocalDate.now().toString();
        }
        recordDateField = new JTextField(existingDate);
        fieldsPanel.add(recordDateField);

        fieldsPanel.add(new JLabel("*Жалобы:"));
        complaintsArea = new JTextArea(3, 20);
        if (existingRecord != null && existingRecord.getComplaints() != null) {
            complaintsArea.setText(existingRecord.getComplaints());
        }
        fieldsPanel.add(new JScrollPane(complaintsArea));

        fieldsPanel.add(new JLabel("*Диагноз:"));
        diagnosisArea = new JTextArea(3, 20);
        if (existingRecord != null && existingRecord.getDiagnosis() != null) {
            diagnosisArea.setText(existingRecord.getDiagnosis());
        }
        fieldsPanel.add(new JScrollPane(diagnosisArea));

        fieldsPanel.add(new JLabel("Назначенное лечение:"));
        treatmentArea = new JTextArea(3, 20);
        if (existingRecord != null && existingRecord.getTreatment() != null) {
            treatmentArea.setText(existingRecord.getTreatment());
        }
        fieldsPanel.add(new JScrollPane(treatmentArea));

        fieldsPanel.add(new JLabel("Дата след. приема:"));
        String existingNext = (existingRecord != null && existingRecord.getNextAppointment() != null)
                ? existingRecord.getNextAppointment() : "";
        nextAppointmentField = new JTextField(existingNext);
        fieldsPanel.add(nextAppointmentField);

        JLabel hintLabel = new JLabel("* Поля, обязательные для заполнения");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        hintLabel.setForeground(Color.GRAY);
        fieldsPanel.add(hintLabel);
        fieldsPanel.add(new JLabel());

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
        if (!recordDateField.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Дата приема должна быть в формате ГГГГ-ММ-ДД","Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (complaintsArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите жалобы", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (diagnosisArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите диагноз", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String nextDate = nextAppointmentField.getText().trim();
        if (!nextDate.isEmpty() && !nextDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Дата следующего приема должна быть в формате ГГГГ-ММ-ДД","Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public Object[] getResult() {
        if (!confirmed) return null;
        String selected = (String) doctorComboBox.getSelectedItem();
        int doctorId = Integer.parseInt(selected.split(" - ")[0]);
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