package view;

import javax.swing.*;
import java.awt.*;

public class PatientDialog extends JDialog {
    private JTextField fullNameField, birthDateField, phoneField, addressField, snilsField;
    private boolean confirmed = false;

    public PatientDialog(JFrame parent, String fullName, String birthDate,
                         String phone, String address, String snils) {
        super(parent, fullName.isEmpty() ? "Добавить пациента" : "Редактировать пациента", true);
        initComponents(fullName, birthDate, phone, address, snils);
        setSize(500, 450);
        setLocationRelativeTo(parent);
    }

    private void initComponents(String fullName, String birthDate,
                                String phone, String address, String snils) {
        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel("*ФИО:"));
        fullNameField = new JTextField(fullName);
        fieldsPanel.add(fullNameField);

        fieldsPanel.add(new JLabel("*Дата рождения (ГГГГ-ММ-ДД):"));
        birthDateField = new JTextField(birthDate);
        fieldsPanel.add(birthDateField);

        fieldsPanel.add(new JLabel("*Телефон:"));
        phoneField = new JTextField(phone);
        fieldsPanel.add(phoneField);

        fieldsPanel.add(new JLabel("*Адрес:"));
        addressField = new JTextField(address);
        fieldsPanel.add(addressField);

        fieldsPanel.add(new JLabel("*СНИЛС (XXX-XXX-XXX XX):"));
        snilsField = new JTextField(snils);
        fieldsPanel.add(snilsField);

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
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ФИО пациента", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (birthDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите дату рождения", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!birthDateField.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Дата рождения должна быть в формате ГГГГ-ММ-ДД","Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите телефон", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (addressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите адрес", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (snilsField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите СНИЛС", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!snilsField.getText().trim().matches("\\d{3}-\\d{3}-\\d{3} \\d{2}")) {
            JOptionPane.showMessageDialog(this, "Формат СНИЛС: XXX-XXX-XXX XX", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public String[] getResult() {
        if (!confirmed) return null;
        return new String[]{
                fullNameField.getText().trim(),
                birthDateField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim(),
                snilsField.getText().trim()
        };
    }
}