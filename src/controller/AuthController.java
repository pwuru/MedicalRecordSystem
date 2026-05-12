package controller;

import model.MedicalDB;
import model.User;
import java.sql.SQLException;

public class AuthController {

    public static User authenticate(String username, String password) {
        try {
            User user = MedicalDB.getUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}