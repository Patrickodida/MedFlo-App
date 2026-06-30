package com.hms.service;

import com.hms.dao.UserDAO;
import com.hms.model.User;

public class AuthService {
    private final UserDAO dao = new UserDAO();
    private static User currentUser;

    public User login(String username, String password) {
        String error = validateInputs(username, password);
        if (error != null) return null;
        User user = dao.authenticate(username.trim(), password);
        if (user != null) currentUser = user;
        return user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public String validateInputs(String username, String password) {
        if (username == null || username.isBlank())
            return "username is required";
        if (password == null || password.isBlank())
            return "password is required";
        if (username.trim().length() < 3)
            return "username should be at least 3 characters";
        return null;
    }

    public String getDashboardPath(String role) {
        return switch (role) {
            case "Admin" -> "/fxml/AdminDashboard.fxml";
            case "Doctor" -> "/fxml/DoctorDashboard.fxml";
            case "Nurse" -> "/fxml/NurseDashboard.fxml";
            case "Receptionist" -> "/fxml/ReceptionistDashboard.fxml";
            case "Pharmacist" -> "/fxml/PharmacistDashboard.fxml";
            default -> "/fxml/AdminDashboard.fxml";
        };
    }
}
