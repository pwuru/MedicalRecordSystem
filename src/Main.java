import view.MainWindow;
import model.MedicalDB;
import javax.swing.*;
import java.sql.SQLException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                MedicalDB.connect();
                System.out.println("База данных успешно подключена");

                MainWindow window = new MainWindow();

                window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                window.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            MedicalDB.closeDB();
                            System.out.println("Соединение с БД закрыто");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        System.exit(0);
                    }
                });

                window.setVisible(true);

            } catch (SQLException e) {
                System.err.println("Ошибка при подключении к базе данных:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Не удалось подключиться к базе данных:\n" + e.getMessage(),
                        "Критическая ошибка",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}