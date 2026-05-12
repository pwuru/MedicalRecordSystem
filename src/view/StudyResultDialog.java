package view;

import model.Doctor;
import model.ExaminationProtocol;
import model.StudyResult;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudyResultDialog extends JDialog {
    private JComboBox<String> doctorComboBox;
    private JComboBox<String> referralComboBox;
    private JComboBox<String> studyTypeComboBox;
    private JTextField recordDateField, resultDateField;
    private JTextArea resultArea;
    private boolean confirmed = false;

    public StudyResultDialog(JFrame parent, int patientId,
                             StudyResult existingRecord,
                             List<Doctor> doctors,
                             List<ExaminationProtocol> protocols) {
        super(parent, existingRecord == null ? "Добавить результат исследования" : "Редактировать результат исследования", true);
        initComponents(existingRecord, doctors, protocols);
        setSize(600, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents(StudyResult existingRecord,
                                List<Doctor> doctors,
                                List<ExaminationProtocol> protocols) {
        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel("*Врач:"));
        doctorComboBox = new JComboBox<>();
        int selectedDoctorIndex = 0;
        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            doctorComboBox.addItem(d.id + " - " + d.fullName + " (" + d.specialization + ")");
            if (existingRecord != null && existingRecord.getDoctorId() == d.id) {
                selectedDoctorIndex = i;
            }
        }
        doctorComboBox.setSelectedIndex(selectedDoctorIndex);
        fieldsPanel.add(doctorComboBox);

        fieldsPanel.add(new JLabel("Дата назначения (ГГГГ-ММ-ДД):"));
        String existingDate = (existingRecord != null && existingRecord.getRecordDate() != null)
                ? existingRecord.getRecordDate() : "";
        recordDateField = new JTextField(existingDate);
        fieldsPanel.add(recordDateField);

        fieldsPanel.add(new JLabel("Направление от протокола:"));
        referralComboBox = new JComboBox<>();
        referralComboBox.addItem("Без направления");

        int selectedReferralIndex = 0;
        int counter = 1;
        for (ExaminationProtocol p : protocols) {
            String displayText = counter++ + ". " + p.getRecordDate() + " - " + p.getDiagnosis();
            referralComboBox.addItem(p.getId() + "|" + displayText);
            if (existingRecord != null && existingRecord.getReferralId() != null &&
                    existingRecord.getReferralId() == p.getId()) {
                selectedReferralIndex = referralComboBox.getItemCount() - 1;
            }
        }
        referralComboBox.setSelectedIndex(selectedReferralIndex);
        fieldsPanel.add(referralComboBox);

        fieldsPanel.add(new JLabel("*Вид исследования:"));
        studyTypeComboBox = new JComboBox<>(new String[]{
                "Анализ крови", "Анализ мочи", "Рентген", "УЗИ", "МРТ", "КТ", "ЭКГ", "Другое"
        });
        if (existingRecord != null && existingRecord.getStudyType() != null) {
            studyTypeComboBox.setSelectedItem(existingRecord.getStudyType());
        }
        fieldsPanel.add(studyTypeComboBox);

        fieldsPanel.add(new JLabel("*Результат:"));
        resultArea = new JTextArea(3, 20);
        if (existingRecord != null && existingRecord.getResult() != null) {
            resultArea.setText(existingRecord.getResult());
        }
        fieldsPanel.add(new JScrollPane(resultArea));

        fieldsPanel.add(new JLabel("*Дата выполнения (ГГГГ-ММ-ДД):"));
        String existingResultDate;
        if (existingRecord != null && existingRecord.getResultDate() != null) {
            existingResultDate = existingRecord.getResultDate();
        } else {
            existingResultDate = java.time.LocalDate.now().toString();
        }
        resultDateField = new JTextField(existingResultDate);
        fieldsPanel.add(resultDateField);

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
        if (studyTypeComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Выберите вид исследования", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (resultArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите результат исследования", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (resultDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите дату выполнения", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!resultDateField.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Дата рождения должна быть в формате ГГГГ-ММ-ДД (например, 1985-05-15)",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public Object[] getResult() {
        if (!confirmed) return null;
        String selectedDoctor = (String) doctorComboBox.getSelectedItem();
        int doctorId = Integer.parseInt(selectedDoctor.split(" - ")[0]);

        Integer referralId = null;
        int referralIndex = referralComboBox.getSelectedIndex();
        if (referralIndex > 0) {
            String selectedReferral = (String) referralComboBox.getSelectedItem();
            referralId = Integer.parseInt(selectedReferral.split("\\|")[0]);
        }

        return new Object[]{
                doctorId,
                recordDateField.getText().trim(),
                referralId,
                studyTypeComboBox.getSelectedItem(),
                resultArea.getText().trim(),
                resultDateField.getText().trim()
        };
    }
}