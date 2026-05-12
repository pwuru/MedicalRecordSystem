package model;
import java.sql.*;
import java.util.*;

public class MedicalDB {
    private static Connection conn;

    public static void connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:medical.db");
        if (!tableExists()) {
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
                    "full_name TEXT, specialization TEXT, " +
                    "user_id INTEGER REFERENCES users(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS examination_protocols (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER REFERENCES patients(id), " +
                    "doctor_id INTEGER REFERENCES doctors(id), " +
                    "record_date TEXT, complaints TEXT, " +
                    "diagnosis TEXT, treatment TEXT, next_appointment TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS study_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER REFERENCES patients(id), " +
                    "doctor_id INTEGER REFERENCES doctors(id), " +
                    "record_date TEXT, referral_id INTEGER, " +
                    "study_type TEXT, result TEXT, result_date TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE, " +
                    "password TEXT, " +
                    "role TEXT CHECK(role IN ('ADMIN', 'DOCTOR', 'PATIENT')), " +
                    "patient_id INTEGER REFERENCES patients(id))");
        }
    }

    public static void addUser(String username, String password, String role, Integer patientId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO users (username, password, role, patient_id) VALUES (?, ?, ?, ?)");
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, role);
        if (patientId != null) {
            pstmt.setInt(4, patientId);
        } else {
            pstmt.setNull(4, java.sql.Types.INTEGER);
        }
        pstmt.executeUpdate();
    }

    public static User getUserByUsername(String username) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ?");
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            Integer patientId = rs.getInt("patient_id");
            if (rs.wasNull()) patientId = null;
            return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    patientId
            );
        }
        return null;
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
                "DELETE FROM examination_protocols WHERE patient_id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM study_results WHERE patient_id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM patients WHERE id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
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

    public static List<Doctor> getDoctors() throws SQLException {
        return getDoctorsWithFilter(null, "ADMIN");
    }

    public static void addDoctor(String name, String specialization) throws SQLException {
        addDoctor(name, specialization, null);
    }

    public static void addDoctor(String name, String specialization, Integer userId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO doctors (full_name, specialization, user_id) VALUES (?, ?, ?)");
        pstmt.setString(1, name);
        pstmt.setString(2, specialization);
        if (userId != null) {
            pstmt.setInt(3, userId);
        } else {
            pstmt.setNull(3, java.sql.Types.INTEGER);
        }
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

    public static Doctor getDoctorByUserId(int userId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM doctors WHERE user_id = ?");
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return new Doctor(rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("specialization"));
        }
        return null;
    }

    public static List<Doctor> getDoctorsWithFilter(Integer currentUserId, String role) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql;
        PreparedStatement pstmt;

        if (role.equals("DOCTOR") && currentUserId != null) {
            sql = "SELECT * FROM doctors WHERE user_id = ? ORDER BY full_name";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
        } else {
            sql = "SELECT * FROM doctors ORDER BY full_name";
            pstmt = conn.prepareStatement(sql);
        }

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            list.add(new Doctor(rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("specialization")));
        }
        return list;
    }

    public static boolean hasDoctorRecords(int doctorId) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM examination_protocols WHERE doctor_id = ?");
        pstmt.setInt(1, doctorId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) return true;

        pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM study_results WHERE doctor_id = ?");
        pstmt.setInt(1, doctorId);
        rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    public static List<ExaminationProtocol> getExaminationProtocolsByPatient(int patientId) throws SQLException {
        List<ExaminationProtocol> list = new ArrayList<>();
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM examination_protocols WHERE patient_id = ? ORDER BY record_date DESC");
        pstmt.setInt(1, patientId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ExaminationProtocol(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("record_date"),
                        rs.getString("complaints"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment"),
                        rs.getString("next_appointment")
                ));
            }
        }
        return list;
    }

    public static ExaminationProtocol getExaminationProtocolById(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM examination_protocols WHERE id = ?");
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return new ExaminationProtocol(
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

    public static void addExaminationProtocol(int patientId, int doctorId, String recordDate,
                                              String complaints, String diagnosis,
                                              String treatment, String nextAppointment) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO examination_protocols (patient_id, doctor_id, record_date, " +
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

    public static void updateExaminationProtocol(int id, int patientId, int doctorId, String recordDate,
                                                 String complaints, String diagnosis,
                                                 String treatment, String nextAppointment) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE examination_protocols SET patient_id=?, doctor_id=?, record_date=?, " +
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

    public static void deleteExaminationProtocol(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM examination_protocols WHERE id = ?");
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    }

    public static List<StudyResult> getStudyResultsByPatient(int patientId) throws SQLException {
        List<StudyResult> list = new ArrayList<>();
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM study_results WHERE patient_id = ? ORDER BY record_date DESC");
        pstmt.setInt(1, patientId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Integer referralId = rs.getInt("referral_id");
                if (rs.wasNull()) referralId = null;
                list.add(new StudyResult(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("record_date"),
                        referralId,
                        rs.getString("study_type"),
                        rs.getString("result"),
                        rs.getString("result_date")
                ));
            }
        }
        return list;
    }

    public static StudyResult getStudyResultById(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM study_results WHERE id = ?");
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            Integer referralId = rs.getInt("referral_id");
            if (rs.wasNull()) referralId = null;
            return new StudyResult(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    rs.getInt("doctor_id"),
                    rs.getString("record_date"),
                    referralId,
                    rs.getString("study_type"),
                    rs.getString("result"),
                    rs.getString("result_date")
            );
        }
        return null;
    }

    public static void addStudyResult(int patientId, int doctorId, String recordDate,
                                      Integer referralId, String studyType,
                                      String result, String resultDate) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO study_results (patient_id, doctor_id, record_date, " +
                        "referral_id, study_type, result, result_date) VALUES (?, ?, ?, ?, ?, ?, ?)");
        pstmt.setInt(1, patientId);
        pstmt.setInt(2, doctorId);
        pstmt.setString(3, recordDate);
        if (referralId != null) {
            pstmt.setInt(4, referralId);
        } else {
            pstmt.setNull(4, java.sql.Types.INTEGER);
        }
        pstmt.setString(5, studyType);
        pstmt.setString(6, result);
        pstmt.setString(7, resultDate);
        pstmt.executeUpdate();
    }

    public static void updateStudyResult(int id, int patientId, int doctorId, String recordDate,
                                         Integer referralId, String studyType,
                                         String result, String resultDate) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE study_results SET patient_id=?, doctor_id=?, record_date=?, " +
                        "referral_id=?, study_type=?, result=?, result_date=? WHERE id=?");
        pstmt.setInt(1, patientId);
        pstmt.setInt(2, doctorId);
        pstmt.setString(3, recordDate);
        if (referralId != null) {
            pstmt.setInt(4, referralId);
        } else {
            pstmt.setNull(4, java.sql.Types.INTEGER);
        }
        pstmt.setString(5, studyType);
        pstmt.setString(6, result);
        pstmt.setString(7, resultDate);
        pstmt.setInt(8, id);
        pstmt.executeUpdate();
    }

    public static void deleteStudyResult(int id) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM study_results WHERE id = ?");
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    }

    public static void closeDB() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    public static boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean tableExists() throws SQLException {
        ResultSet rs = conn.getMetaData().getTables(null, null, "patients", null);
        return rs.next();
    }

    private static void insertSampleData() throws SQLException {
        addDoctor("Иванов Иван Иванович", "Терапевт", 2);
        addDoctor("Петрова Анна Сергеевна", "Кардиолог", null);
        addDoctor("Сидоров Петр Васильевич", "Хирург", null);

        addPatient("Сидоров Василий Петрович", "1980-05-15", "89123456789",
                "г. Иркутск, ул. Ленина, д. 10", "123-456-789 01");
        addPatient("Кузнецова Мария Ивановна", "1992-08-23", "891456789011",
                "г. Иркутск, ул. Советская, д. 25", "234-567-890 02");

        addExaminationProtocol(1, 1, "2025-01-20",
                "Кашель, температура 38.5, головная боль",
                "ОРВИ",
                "Постельный режим, обильное питье",
                "2025-01-27");

        addExaminationProtocol(1, 2, "2025-02-10",
                "Жалобы на периодические боли в груди, одышку",
                "Артериальная гипертензия 2 степени",
                "Контроль давления, диета с ограничением соли",
                "2025-03-10");

        addExaminationProtocol(2, 3, "2025-02-05",
                "Боль в правом колене, отёк",
                "Артроз коленного сустава",
                "Физиотерапия, ЛФК",
                "2025-03-05");

        addStudyResult(1, 1, "2025-01-21", 1, "Анализ крови",
                "Гемоглобин 145, лейкоциты 6.5", "2025-01-22");
        addStudyResult(1, 1, "2025-01-25", null, "Рентген грудной клетки",
                "Без патологических изменений", "2025-01-26");

        addUser("admin", "admin123", "ADMIN", null);
        addUser("ivanov", "doc123", "DOCTOR", null);
        addUser("sidorov", "pat123", "PATIENT", 1);
    }
}