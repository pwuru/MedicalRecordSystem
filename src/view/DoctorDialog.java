package view;

import javax.swing.*;
import java.awt.*;

public class DoctorDialog extends JDialog {
    private JTextField fullNameField, specializationField;
    private boolean confirmed = false;

    public DoctorDialog(JFrame parent, String fullName, String specialization) {
        super(parent, fullName.isEmpty() ? "Добавить врача" : "Редактировать врача", true);
        initComponents(fullName, specialization);
        setSize(500, 350);
        setLocationRelativeTo(parent);
    }

    private void initComponents(String fullName, String specialization) {
        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(new JLabel("*ФИО врача:"));
        fullNameField = new JTextField(fullName, 20);
        fieldsPanel.add(fullNameField);

        fieldsPanel.add(new JLabel("*Специализация:"));
        specializationField = new JTextField(specialization, 20);
        fieldsPanel.add(specializationField);

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
            JOptionPane.showMessageDialog(this, "Введите ФИО врача", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (specializationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите специализацию врача", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public String[] getResult() {
        if (!confirmed) return null;
        return new String[]{
                fullNameField.getText().trim(),
                specializationField.getText().trim()
        };
    }
}