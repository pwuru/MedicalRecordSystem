package model;
import java.sql.*;
import java.util.*;

public class MedicalDB {
    private static Connection conn;

    public static void connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:medical.db");
        if (!tableExists("patients")) {
            createTables();
            insertSampleData();
        }
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "full_name TEXT, birth_date TEXT, phone TEXT, " +
                    "address TEXT, snils TEXT UNIQUE)");

            stmt.execute("CREATE TABLE doctors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "full_name TEXT, specialization TEXT)");

            stmt.execute("CREATE TABLE medical_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER REFERENCES patients(id), " +
                    "doctor_id INTEGER REFERENCES doctors(id), " +
                    "record_date TEXT, complaints TEXT, " +
                    "diagnosis TEXT, treatment TEXT, next_appointment TEXT)");
        }
    }

    public static List<Patient> getPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM patients ORDER BY full_name")) {
            while (rs.next()) {
                list.add(new Patient(rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("birth_date"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("snils")));
            }
        }
        return list;
    }

    public static Patient getPatientById(int patientId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM patients WHERE id = ?");
        pstmt.setInt(1, patientId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return new Patient(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("birth_date"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getString("snils")
            );
        }
        return null;
    }

    public static void addPatient(String name, String birthDate,
                                  String phone, String address, String snils)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO patients (full_name, birth_date, phone, address, snils) " +
                        "VALUES (?, ?, ?, ?, ?)");
        pstmt.setString(1, name);
        pstmt.setString(2, birthDate);
        pstmt.setString(3, phone);
        pstmt.setString(4, address);
        pstmt.setString(5, snils);
        pstmt.executeUpdate();
    }

    public static void updatePatient(int id, String name, String birthDate,
                                     String phone, String address, String snils)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE patients SET full_name=?, birth_date=?, phone=?, address=?, snils=? WHERE id=?");
        pstmt.setString(1, name);
        pstmt.setString(2, birthDate);
        pstmt.setString(3, phone);
        pstmt.setString(4, address);
        pstmt.setString(5, snils);
        pstmt.setInt(6, id);
        pstmt.executeUpdate();
    }

    public static void deletePatient(int id) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM medical_records WHERE patient_id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM patients WHERE id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public static List<Doctor> getDoctors() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM doctors ORDER BY full_name")) {
            while (rs.next()) {
                list.add(new Doctor(rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("specialization")));
            }
        }
        return list;
    }

    public static void addDoctor(String name, String specialization) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO doctors (full_name, specialization) VALUES (?, ?)");
        pstmt.setString(1, name);
        pstmt.setString(2, specialization);
        pstmt.executeUpdate();
    }

    public static void updateDoctor(int id, String name, String specialization) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE doctors SET full_name=?, specialization=? WHERE id=?");
        pstmt.setString(1, name);
        pstmt.setString(2, specialization);
        pstmt.setInt(3, id);
        pstmt.executeUpdate();
    }

    public static void deleteDoctor(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM doctors WHERE id = ?");
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    }

    public static String getDoctorName(int doctorId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT full_name FROM doctors WHERE id = ?");
        pstmt.setInt(1, doctorId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("full_name");
        }
        return "Неизвестный врач";
    }

    public static List<MedicalRecord> getRecordsByPatient(int patientId) throws SQLException {
        List<MedicalRecord> list = new ArrayList<>();
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM medical_records WHERE patient_id = ? ORDER BY record_date DESC");
        pstmt.setInt(1, patientId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new MedicalRecord(rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("record_date"),
                        rs.getString("complaints"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment"),
                        rs.getString("next_appointment")));
            }
        }
        return list;
    }

    public static void addMedicalRecord(int patientId, int doctorId,
                                        String recordDate, String complaints,
                                        String diagnosis, String treatment,
                                        String nextAppointment) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO medical_records (patient_id, doctor_id, record_date, " +
                        "complaints, diagnosis, treatment, next_appointment) VALUES (?, ?, ?, ?, ?, ?, ?)");
        pstmt.setInt(1, patientId);
        pstmt.setInt(2, doctorId);
        pstmt.setString(3, recordDate);
        pstmt.setString(4, complaints);
        pstmt.setString(5, diagnosis);
        pstmt.setString(6, treatment);
        pstmt.setString(7, nextAppointment);
        pstmt.executeUpdate();
    }

    public static MedicalRecord getRecordById(int recordId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM medical_records WHERE id = ?");
        pstmt.setInt(1, recordId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return new MedicalRecord(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    rs.getInt("doctor_id"),
                    rs.getString("record_date"),
                    rs.getString("complaints"),
                    rs.getString("diagnosis"),
                    rs.getString("treatment"),
                    rs.getString("next_appointment")
            );
        }
        return null;
    }

    public static void updateMedicalRecord(int id, int patientId, int doctorId,
                                           String recordDate, String complaints,
                                           String diagnosis, String treatment,
                                           String nextAppointment) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE medical_records SET patient_id=?, doctor_id=?, record_date=?, " +
                        "complaints=?, diagnosis=?, treatment=?, next_appointment=? WHERE id=?");
        pstmt.setInt(1, patientId);
        pstmt.setInt(2, doctorId);
        pstmt.setString(3, recordDate);
        pstmt.setString(4, complaints);
        pstmt.setString(5, diagnosis);
        pstmt.setString(6, treatment);
        pstmt.setString(7, nextAppointment);
        pstmt.setInt(8, id);
        pstmt.executeUpdate();
    }

    public static void deleteMedicalRecord(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM medical_records WHERE id = ?");
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    }

    public static String getPatientName(int patientId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT full_name FROM patients WHERE id = ?");
        pstmt.setInt(1, patientId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("full_name");
        }
        return "Неизвестный пациент";
    }

    public static boolean patientExists(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM patients WHERE id = ?");
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    public static boolean doctorExists(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM doctors WHERE id = ?");
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    public static boolean hasDoctorRecords(int doctorId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM medical_records WHERE doctor_id = ?");
        pstmt.setInt(1, doctorId);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    public static void closeDB() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    private static boolean tableExists(String tableName) throws SQLException {
        ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
        return rs.next();
    }

    private static void insertSampleData() throws SQLException {
        addDoctor("Иванов Иван Иванович", "Терапевт");
        addDoctor("Петрова Анна Сергеевна", "Кардиолог");
        addDoctor("Сидоров Петр Васильевич", "Хирург");

        addPatient("Сидоров Василий Петрович", "1980-05-15", "89123456789",
                "г. Иркутск, ул. Ленина, д. 10", "123-456-789 01");
        addPatient("Кузнецова Мария Ивановна", "1992-08-23", "891456789011",
                "г. Иркутск, ул. Советская, д. 25", "234-567-890 02");

        addMedicalRecord(1, 1, "2025-01-20",
                "Кашель, температура 38.5, головная боль",
                "ОРВИ",
                "Постельный режим, обильное питье",
                "2025-01-27");

        addMedicalRecord(1, 2, "2025-02-10",
                "Жалобы на периодические боли в груди, одышку",
                "Артериальная гипертензия 2 степени",
                "Контроль давления, диета с ограничением соли",
                "2025-03-10");

        addMedicalRecord(2, 3, "2025-02-05",
                "Боль в правом колене, отёк",
                "Артроз коленного сустава",
                "Физиотерапия, ЛФК",
                "2025-03-05");
    }
}
