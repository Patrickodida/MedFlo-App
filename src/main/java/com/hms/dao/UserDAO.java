package com.hms.dao;

import com.hms.model.User;
import com.hms.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // CREATE a new User
    public int createUser(User u) {
        String sql = """
                INSERT INTO users (username, password, full_name, email, role_id)
                VALUES (?, ?, ?, ?, ?,
                ( SELECT role_id FROM roles WHERE role_name = ?))
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getRole());

            int rows = ps.executeUpdate();
            if(rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if(keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("createUser error: " + e.getMessage());
        }
        return -1;
    }

    // READ - Authenticate Login
    public User authenticate(String username, String password) {
        String sql = """
                SELECT u.user_id, u.username, u.password, u.full_name,
                        u.email, r.role_name
                FROM   users u
                JOIN   roles r ON u.role_id = r.role_id
                WHERE  u.username = ? AND u.is_active = 1
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String stored = rs.getString("password");
                // Plain text check — replace with BCrypt.checkpw(password, stored)
                if (password.equals(stored)) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role_name"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("authenticate error: " + e.getMessage());
        }
        return null;
    }

    // READ - All users
    public List <User> getAllUsers() {
        List <User> list = new ArrayList<>();
        String sql = """
                SELECT u.user_id, u.username, u.full_name, u.email,
                       r.role_name, u.is_active
                FROM   users u
                JOIN   roles r ON u.role_id = r.role_id
                ORDER  BY u.full_name
                """;
        try (Connection conn = DBConnection.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role_name"));
                u.setActive(rs.getBoolean("is_active"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("getAllUsers error: " + e.getMessage());
        }
        return list;
}

// UPDATE - Change user details or password
    public boolean updateUser(User u) {
        String sql = """
                UPDATE users
                SET full_name = ?, email = ?,
                      role_id = (SELECT role_id FROM roles WHERE role_name = ?)
                WHERE user_id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getRole());
            ps.setInt   (4, u.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateUser error: " + e.getMessage());
        }
        return false;
    }

    public boolean resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("resetPassword error: " + e.getMessage());
        }
        return false;
    }

    // DELETE - Deactivate Account
    public boolean deActivateUser(int userId) {
        String sql = "UPDATE users SET is_active = 0 WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deActivateUser error: " + e.getMessage());
        }
        return false;
    }

    // RE-Activate a De-Activate user/ Account
    public boolean activateUser(int userId) {
        String sql = "UPDATE users SET is_active = 1 WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("activateUser error: " + e.getMessage());
        }
        return false;
    }
}
