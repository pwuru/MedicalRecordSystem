package view;

import model.MedicalDB;
import model.MedicalRecord;
import model.Patient;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class HistoryViewDialog extends JDialog {
    private int patientId;

    public HistoryViewDialog(JFrame parent, int patientId) {
        super(parent, "История болезни пациента", true);
        this.patientId = patientId;
        initComponents();
        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        try {
            Patient patient = MedicalDB.getPatientById(patientId);
            if (patient == null) {
                JOptionPane.showMessageDialog(this, "Пациент не найден", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            List<MedicalRecord> records = MedicalDB.getRecordsByPatient(patientId);

            JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
            infoPanel.setBorder(BorderFactory.createTitledBorder("Информация о пациенте"));
            infoPanel.add(new JLabel("ФИО:"));
            infoPanel.add(new JLabel(patient.fullName));
            infoPanel.add(new JLabel("Дата рождения:"));
            infoPanel.add(new JLabel(patient.birthDate));
            infoPanel.add(new JLabel("СНИЛС:"));
            infoPanel.add(new JLabel(patient.snils != null && !patient.snils.isEmpty() ?
                    patient.snils : "не указан"));

            add(infoPanel, BorderLayout.NORTH);

            JPanel recordsPanel = new JPanel(new BorderLayout());
            recordsPanel.setBorder(BorderFactory.createTitledBorder("Записи в истории болезни"));

            if (records.isEmpty()) {
                JLabel emptyLabel = new JLabel("Нет записей в истории болезни", JLabel.CENTER);
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                emptyLabel.setForeground(Color.GRAY);
                recordsPanel.add(emptyLabel, BorderLayout.CENTER);
            } else {
                String[] columnNames = {"№", "Дата", "Врач", "Диагноз", "Лечение", "След. прием"};
                Object[][] data = new Object[records.size()][6];

                for (int i = 0; i < records.size(); i++) {
                    MedicalRecord r = records.get(i);
                    String doctorName = MedicalDB.getDoctorName(r.doctorId);
                    data[i][0] = i + 1;
                    data[i][1] = r.recordDate;
                    data[i][2] = doctorName;
                    data[i][3] = r.diagnosis;
                    data[i][4] = r.treatment != null && r.treatment.length() > 30 ?
                            r.treatment.substring(0, 27) + "..." : r.treatment;
                    data[i][5] = r.nextAppointment != null && !r.nextAppointment.isEmpty() ?
                            r.nextAppointment : "не назначен";
                }

                JTable recordsTable = new JTable(data, columnNames);
                recordsTable.setRowHeight(25);
                recordsTable.getTableHeader().setReorderingAllowed(false);

                JScrollPane scrollPane = new JScrollPane(recordsTable);
                recordsPanel.add(scrollPane, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JButton viewDetailsBtn = new JButton("Детали");
                JButton closeBtn = new JButton("Закрыть");

                viewDetailsBtn.addActionListener(e -> {
                    int selectedRow = recordsTable.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(this, "Выберите запись");
                        return;
                    }
                    showRecordDetails(records.get(selectedRow));
                });

                closeBtn.addActionListener(e -> dispose());

                buttonPanel.add(viewDetailsBtn);
                buttonPanel.add(closeBtn);
                recordsPanel.add(buttonPanel, BorderLayout.SOUTH);
            }

            add(recordsPanel, BorderLayout.CENTER);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки истории: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void showRecordDetails(MedicalRecord record) {
        try {
            String doctorName = MedicalDB.getDoctorName(record.doctorId);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            StringBuilder sb = new StringBuilder();
            sb.append("ДЕТАЛИ ЗАПИСИ:\n\n");
            sb.append("Дата приема: ").append(record.recordDate).append("\n");
            sb.append("Врач: ").append(doctorName).append("\n\n");
            sb.append("ЖАЛОБЫ:\n");
            sb.append(record.complaints != null && !record.complaints.isEmpty() ?
                    record.complaints : "не указаны").append("\n\n");
            sb.append("ДИАГНОЗ:\n");
            sb.append(record.diagnosis).append("\n\n");
            sb.append("НАЗНАЧЕННОЕ ЛЕЧЕНИЕ:\n");
            sb.append(record.treatment != null && !record.treatment.isEmpty() ?
                    record.treatment : "не указано").append("\n\n");
            sb.append("СЛЕДУЮЩИЙ ПРИЕМ:\n");
            sb.append(record.nextAppointment != null && !record.nextAppointment.isEmpty() ?
                    record.nextAppointment : "не назначен").append("\n");

            textArea.setText(sb.toString());

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Детали записи от " + record.recordDate,
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}