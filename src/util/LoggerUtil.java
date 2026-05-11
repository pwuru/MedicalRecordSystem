package util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    private static final String LOG_DIR = "logs";

    static {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdir();
        }
    }

    public static void log(int patientId, String patientName, String action, String details) {
        String fileName = LOG_DIR + File.separator + "patient_" + patientId + "_" + patientName.replaceAll("[^a-zA-Zа-яА-Я0-9]", "_") + ".txt";

        try (FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out.println("[" + timestamp + "] " + action);
            out.println("  " + details);
            out.println("----------------------------------------");

        } catch (IOException e) {
            System.err.println("Ошибка записи лога: " + e.getMessage());
        }
    }
}