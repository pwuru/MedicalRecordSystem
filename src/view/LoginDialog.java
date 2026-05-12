package view;

import model.User;
import controller.AuthController;
import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean authenticated = false;
    private User currentUser;

    public LoginDialog(JFrame parent) {
        super(parent, "Авторизация", true);
        initComponents();
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        fieldsPanel.add(new JLabel("Логин:"));
        usernameField = new JTextField(15);
        fieldsPanel.add(usernameField);

        fieldsPanel.add(new JLabel("Пароль:"));
        passwordField = new JPasswordField(15);
        fieldsPanel.add(passwordField);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginBtn = new JButton("Войти");
        JButton cancelBtn = new JButton("Отмена");

        loginBtn.addActionListener(e -> login());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginBtn);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Введите логин и пароль",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = AuthController.authenticate(username, password);
        if (user != null) {
            authenticated = true;
            currentUser = user;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Неверный логин или пароль",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}