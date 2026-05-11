package view;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import model.*;
import controller.*;
import java.sql.*;
import java.awt.Desktop;
import java.io.File;

public class MainWindow extends JFrame {
    private JTable patientsTable, recordsTable, doctorsTable;
    private DefaultTableModel patientsModel, recordsModel, doctorsModel;
    private PatientController patientController;
    private MedicalRecordController recordController;
    private DoctorController doctorController;
    private JLabel statusLabel;
    private JComboBox<String> patientComboBox;
    private int currentPatientId = -1;
    private int currentRecordType = 0; // 0 - протоколы, 1 - исследования

    public MainWindow() {
        setTitle("Медицинская информационная система");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(1200, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Пациенты", createPatientsPanel());
        tabbedPane.addTab("Мед. записи", createRecordsPanel());
        tabbedPane.addTab("Врачи", createDoctorsPanel());

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                refreshPatientComboBox();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        patientController = new PatientController(this);
        recordController = new MedicalRecordController(this);
        doctorController = new DoctorController(this);

        setVisible(true);
    }

    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        patientsModel = new DefaultTableModel(
                new String[]{"ID", "ФИО", "Дата рождения", "Телефон", "Адрес", "СНИЛС"}, 0);
        patientsTable = new JTable(patientsModel);
        patientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(patientsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список пациентов"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Добавить");
        JButton editBtn = new JButton("Изменить");
        JButton deleteBtn = new JButton("Удалить");

        addBtn.addActionListener(e -> patientController.addPatient());
        editBtn.addActionListener(e -> patientController.editPatient());
        deleteBtn.addActionListener(e -> patientController.deletePatient());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel northContainer = new JPanel(new GridLayout(2, 1));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Выберите пациента:"));
        patientComboBox = new JComboBox<>();
        patientComboBox.setPreferredSize(new Dimension(300, 25));
        patientComboBox.addActionListener(e -> {
            int selectedIndex = patientComboBox.getSelectedIndex();
            if (selectedIndex >= 0 && patientComboBox.getItemCount() > 0) {
                String selected = (String) patientComboBox.getSelectedItem();
                String[] parts = selected.split(" - ");
                if (parts.length >= 2) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        currentPatientId = id;
                        if (recordController != null) {
                            recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
                        }
                        updateStatus("Выбран пациент: " + parts[1]);
                    } catch (NumberFormatException ex) {
                        currentPatientId = -1;
                        recordsModel.setRowCount(0);
                    }
                }
            }
        });
        topPanel.add(patientComboBox);
        northContainer.add(topPanel);

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton protocolsRadio = new JRadioButton("Протоколы осмотров", true);
        JRadioButton studiesRadio = new JRadioButton("Результаты исследований", false);
        ButtonGroup group = new ButtonGroup();
        group.add(protocolsRadio);
        group.add(studiesRadio);
        togglePanel.add(protocolsRadio);
        togglePanel.add(studiesRadio);
        northContainer.add(togglePanel);

        panel.add(northContainer, BorderLayout.NORTH);

        recordsModel = new DefaultTableModel(
                new String[]{"ID", "Дата", "Врач", "Диагноз/Исследование", "Дополнительно"}, 0);
        recordsTable = new JTable(recordsModel);
        JScrollPane scrollPane = new JScrollPane(recordsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Записи"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Добавить");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        JButton logBtn = new JButton("Лог");

        protocolsRadio.addActionListener(e -> {
            currentRecordType = 0;
            setupTableForProtocols();
            if (currentPatientId != -1 && recordController != null) {
                recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
            }
        });

        studiesRadio.addActionListener(e -> {
            currentRecordType = 1;
            setupTableForStudies();
            if (currentPatientId != -1 && recordController != null) {
                recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
            }
        });

        addBtn.addActionListener(e -> {
            if (currentPatientId != -1 && recordController != null) {
                recordController.addRecord(currentPatientId, currentRecordType);
            } else {
                showError("Сначала выберите пациента");
            }
        });

        editBtn.addActionListener(e -> {
            if (currentPatientId != -1 && recordController != null) {
                int selectedRow = recordsTable.getSelectedRow();
                if (selectedRow != -1) {
                    recordController.editRecord(currentPatientId, currentRecordType);
                } else {
                    showError("Сначала выберите запись");
                }
            } else {
                showError("Сначала выберите пациента");
            }
        });

        deleteBtn.addActionListener(e -> {
            if (currentPatientId != -1) {
                int selectedRow = recordsTable.getSelectedRow();
                if (selectedRow != -1) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Удалить выбранную запись?", "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION && recordController != null) {
                        recordController.deleteRecord(currentPatientId, currentRecordType);
                    }
                } else {
                    showError("Сначала выберите запись");
                }
            } else {
                showError("Сначала выберите пациента");
            }
        });

        logBtn.addActionListener(e -> {
            if (currentPatientId != -1) {
                try {
                    String patientName = MedicalDB.getPatientName(currentPatientId);
                    String safeName = patientName.replaceAll("[^a-zA-Zа-яА-Я0-9]", "_");
                    String logFileName = "logs/patient_" + currentPatientId + "_" + safeName + ".txt";
                    File logFile = new File(logFileName);

                    if (logFile.exists()) {
                        Desktop.getDesktop().open(logFile);
                        updateStatus("Открыт лог пациента: " + patientName);
                    } else {
                        showError("Файл лога не найден. Возможно, ещё нет записей для этого пациента.");
                    }
                } catch (Exception ex) {
                    showError("Ошибка открытия лога: " + ex.getMessage());
                }
            } else {
                showError("Сначала выберите пациента");
            }
        });

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(logBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        setupTableForProtocols();

        return panel;
    }

    private void setupTableForProtocols() {
        recordsModel.setColumnCount(0);
        recordsModel.setColumnIdentifiers(new String[]{"ID", "Дата", "Врач", "Жалобы", "Диагноз"});
    }

    private void setupTableForStudies() {
        recordsModel.setColumnCount(0);
        recordsModel.setColumnIdentifiers(new String[]{"ID", "Дата", "Врач", "Вид исследования", "Результат"});
    }

    private JPanel createDoctorsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        doctorsModel = new DefaultTableModel(
                new String[]{"ID", "ФИО врача", "Специализация"}, 0);
        doctorsTable = new JTable(doctorsModel);
        JScrollPane scrollPane = new JScrollPane(doctorsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список врачей"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Добавить");
        JButton editBtn = new JButton("Изменить");
        JButton deleteBtn = new JButton("Удалить");

        addBtn.addActionListener(e -> doctorController.addDoctor());
        editBtn.addActionListener(e -> doctorController.editDoctor());
        deleteBtn.addActionListener(e -> doctorController.deleteDoctor());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Готов к работе");
        panel.add(statusLabel);
        return panel;
    }

    public void refreshPatientComboBox() {
        try {
            List<Patient> patients = MedicalDB.getPatients();

            int selectedId = -1;
            if (patientComboBox.getSelectedIndex() >= 0 && patientComboBox.getItemCount() > 0) {
                String selected = (String) patientComboBox.getSelectedItem();
                if (selected != null && !selected.isEmpty()) {
                    try {
                        selectedId = Integer.parseInt(selected.split(" - ")[0]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        selectedId = -1;
                    }
                }
            }

            patientComboBox.removeAllItems();

            if (patients.isEmpty()) {
                patientComboBox.setEnabled(false);
                currentPatientId = -1;
                recordsModel.setRowCount(0);
                updateStatus("Нет зарегистрированных пациентов");
            } else {
                patientComboBox.setEnabled(true);
                for (Patient p : patients) {
                    patientComboBox.addItem(p.id + " - " + p.fullName);
                }

                if (selectedId != -1) {
                    for (int i = 0; i < patientComboBox.getItemCount(); i++) {
                        String item = patientComboBox.getItemAt(i);
                        if (item.startsWith(selectedId + " -")) {
                            patientComboBox.setSelectedIndex(i);
                            currentPatientId = selectedId;
                            if (recordController != null) {
                                recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
                            }
                            updateStatus("Выбран пациент: " + item.split(" - ")[1]);
                            return;
                        }
                    }
                }
                patientComboBox.setSelectedIndex(0);
                String first = (String) patientComboBox.getSelectedItem();
                int firstId = Integer.parseInt(first.split(" - ")[0]);
                currentPatientId = firstId;
                if (recordController != null) {
                    recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
                }
                updateStatus("Выбран пациент: " + first.split(" - ")[1]);
            }
        } catch (SQLException e) {
            showError("Ошибка загрузки пациентов: " + e.getMessage());
        }
    }

    public void refreshCurrentRecords() {
        if (recordController != null && currentPatientId != -1) {
            recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
        }
    }

    public void showDetailsDialog(String details, String title) {
        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public JTable getPatientsTable() { return patientsTable; }
    public JTable getRecordsTable() { return recordsTable; }
    public JTable getDoctorsTable() { return doctorsTable; }
    public DefaultTableModel getPatientsModel() { return patientsModel; }
    public DefaultTableModel getRecordsModel() { return recordsModel; }
    public DefaultTableModel getDoctorsModel() { return doctorsModel; }

    public void updateStatus(String text) { statusLabel.setText(text); }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Успех", JOptionPane.INFORMATION_MESSAGE);
    }
}