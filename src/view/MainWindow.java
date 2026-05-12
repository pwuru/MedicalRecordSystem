package view;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
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
    private int currentRecordType = 0;
    private User currentUser;
    private JButton addPatientBtn, editPatientBtn, deletePatientBtn;
    private JButton addDoctorBtn, editDoctorBtn, deleteDoctorBtn;

    public MainWindow(User user) {
        this.currentUser = user;
        setTitle("Медицинская информационная система - " + user.getUsername() + " (" + user.getRole() + ")");
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
                patientController.refreshPatientComboBox();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        patientController = new PatientController(this);
        recordController = new MedicalRecordController(this);
        doctorController = new DoctorController(this);

        if (currentPatientId != -1 && recordController != null) {
            recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
        }

        setupAccessByRole();

        setVisible(true);
    }

    private void setupAccessByRole() {
        String role = currentUser.getRole();

        switch (role) {
            case "ADMIN":
                updateStatus("Выполнен вход под ролью Администратор");
                break;
            case "DOCTOR":
                disableDoctorManagement();
                updateStatus("Выполнен вход под ролью Врач");
                break;
            case "PATIENT":
                disablePatientAccess();
                updateStatus("Выполнен вход под ролью Пациент");
                break;
        }
    }

    private void disableDoctorManagement() {
        if (addDoctorBtn != null) addDoctorBtn.setEnabled(false);
        if (editDoctorBtn != null) editDoctorBtn.setEnabled(false);
        if (deleteDoctorBtn != null) deleteDoctorBtn.setEnabled(false);
        if (deletePatientBtn != null) deletePatientBtn.setEnabled(false);
    }

    private void disablePatientAccess() {
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
        tabbedPane.setEnabledAt(2, false);

        if (addPatientBtn != null) addPatientBtn.setEnabled(false);
        if (editPatientBtn != null) editPatientBtn.setEnabled(false);
        if (deletePatientBtn != null) deletePatientBtn.setEnabled(false);
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
        addPatientBtn = new JButton("Добавить");
        editPatientBtn = new JButton("Изменить");
        deletePatientBtn = new JButton("Удалить");

        addPatientBtn.addActionListener(e -> patientController.addPatient());
        editPatientBtn.addActionListener(e -> patientController.editPatient());
        deletePatientBtn.addActionListener(e -> patientController.deletePatient());

        buttonPanel.add(addPatientBtn);
        buttonPanel.add(editPatientBtn);
        buttonPanel.add(deletePatientBtn);
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
        patientComboBox.addActionListener(e -> syncPatientSelection());
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

        recordsModel = new DefaultTableModel();
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
            if (recordController != null) {
                recordController.setupTableForProtocols(recordsModel);
            }
            if (currentPatientId != -1 && recordController != null) {
                recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
            }
        });

        studiesRadio.addActionListener(e -> {
            currentRecordType = 1;
            if (recordController != null) {
                recordController.setupTableForStudies(recordsModel);
            }
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
                String patientName = patientController.getPatientName(currentPatientId);
                String safeName = patientName.replaceAll("[^a-zA-Zа-яА-Я0-9]", "_");
                String logFileName = "logs/patient_" + currentPatientId + "_" + safeName + ".txt";
                File logFile = new File(logFileName);

                if (logFile.exists()) {
                    try {
                        Desktop.getDesktop().open(logFile);
                        updateStatus("Открыт лог пациента: " + patientName);
                    } catch (IOException ex) {
                        showError("Ошибка открытия лога: " + ex.getMessage());
                    }
                } else {
                    showError("Файл лога не найден.");
                }
            } else {
                showError("Сначала выберите пациента");
            }
        });

        if (currentUser.getRole().equals("PATIENT")) {
            addBtn.setEnabled(false);
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            patientComboBox.setEnabled(false);

            if (currentUser.getPatientId() != null) {
                int patientIdFromUser = currentUser.getPatientId();
                for (int i = 0; i < patientComboBox.getItemCount(); i++) {
                    String item = patientComboBox.getItemAt(i);
                    if (item.startsWith(patientIdFromUser + " -")) {
                        patientComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(logBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(protocolsRadio::doClick);

        return panel;
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
        addDoctorBtn = new JButton("Добавить");
        editDoctorBtn = new JButton("Изменить");
        deleteDoctorBtn = new JButton("Удалить");

        addDoctorBtn.addActionListener(e -> doctorController.addDoctor());
        editDoctorBtn.addActionListener(e -> doctorController.editDoctor());
        deleteDoctorBtn.addActionListener(e -> doctorController.deleteDoctor());

        buttonPanel.add(addDoctorBtn);
        buttonPanel.add(editDoctorBtn);
        buttonPanel.add(deleteDoctorBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Готов к работе");
        panel.add(statusLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = new JButton("Выход");
        logoutBtn.addActionListener(e -> logout());
        rightPanel.add(logoutBtn);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    public void updatePatientComboBox(List<Patient> patients, int selectedPatientId) {
        patientComboBox.removeAllItems();
        if (patients.isEmpty()) {
            patientComboBox.setEnabled(false);
            patientComboBox.addItem("-- Нет пациентов --");
        } else {
            patientComboBox.setEnabled(true);
            for (Patient p : patients) {
                patientComboBox.addItem(p.id + " - " + p.fullName);
            }

            if (selectedPatientId != -1) {
                for (int i = 0; i < patientComboBox.getItemCount(); i++) {
                    String item = patientComboBox.getItemAt(i);
                    if (item.startsWith(selectedPatientId + " -")) {
                        patientComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    public void syncPatientSelection() {
        int selectedIndex = patientComboBox.getSelectedIndex();
        if (selectedIndex >= 0 && patientComboBox.getItemCount() > 0) {
            String selected = (String) patientComboBox.getSelectedItem();
            if (selected != null && !selected.equals("-- Нет пациентов --")) {
                String[] parts = selected.split(" - ");
                if (parts.length >= 2) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        if (currentPatientId != id) {
                            currentPatientId = id;
                            if (recordController != null) {
                                recordController.loadRecordsForPatient(currentPatientId, currentRecordType);
                            }
                            updateStatus("Выбран пациент: " + parts[1]);
                        }
                    } catch (NumberFormatException ex) {}
                }
            }
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите выйти из системы?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();

            SwingUtilities.invokeLater(() -> {
                try {
                    if (recordController.isDatabaseConnected()) {
                        LoginDialog loginDialog = new LoginDialog(null);
                        loginDialog.setLocationRelativeTo(null);
                        loginDialog.setVisible(true);

                        if (loginDialog.isAuthenticated()) {
                            User newUser = loginDialog.getCurrentUser();
                            MainWindow newWindow = new MainWindow(newUser);
                            newWindow.setVisible(true);
                        } else {
                            System.exit(0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            });
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public MedicalRecordController getRecordController() {
        return recordController;
    }

    public int getCurrentRecordType() {
        return currentRecordType;
    }

    public void setCurrentPatientId(int id) {
        this.currentPatientId = id;
    }

    public int getCurrentPatientId() {
        return currentPatientId;
    }

    public void clearRecordsTable() {
        recordsModel.setRowCount(0);
    }

    public JComboBox<String> getPatientsComboBox() {
        return patientComboBox;
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