package com.hms.dao;
import com.hms.model.Appointment;
import com.hms.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    // CREATE
    public int bookAppointment(Appointment a) {
        String sql = """
            INSERT INTO appointments
              (patient_id, doctor_id, appointment_date, appointment_time, reason, status)
            VALUES (?,?,?,?,?,'Scheduled')
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, a.getPatientId());
            ps.setInt   (2, a.getDoctorId());
            ps.setDate  (3, Date.valueOf(a.getDate()));
            ps.setTime  (4, Time.valueOf(a.getTime()));
            ps.setString(5, a.getReason());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("bookAppointment error: " + e.getMessage());
        }
        return -1;
    }

    // READ
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*,
                   CONCAT(p.first_name,' ',p.last_name) AS patient_name,
                   u.full_name AS doctor_name
            FROM   appointments a
            JOIN   patients p ON a.patient_id = p.patient_id
            JOIN   doctors  d ON a.doctor_id  = d.doctor_id
            JOIN   users    u ON d.user_id    = u.user_id
            ORDER  BY a.appointment_date DESC, a.appointment_time DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAllAppointments error: " + e.getMessage());
        }
        return list;
    }

    // Today's appointments for a specific doctor. Used in Doctor Dashboard.
    public List<Appointment> getTodaysAppointments(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*,
                   CONCAT(p.first_name,' ',p.last_name) AS patient_name,
                   u.full_name AS doctor_name
            FROM   appointments a
            JOIN   patients p ON a.patient_id = p.patient_id
            JOIN   doctors  d ON a.doctor_id  = d.doctor_id
            JOIN   users    u ON d.user_id    = u.user_id
            WHERE  a.doctor_id = ? AND a.appointment_date = CAST(GETDATE() AS DATE)
            ORDER  BY a.appointment_time
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getTodaysAppointments error: " + e.getMessage());
        }
        return list;
    }

    // Count of today's appointments — used in Admin Dashboard stat card.
    public int getTodaysAppointmentCount() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("getTodaysAppointmentCount error: " + e.getMessage());
        }
        return 0;
    }

    // Check if a time slot is already taken before booking.
    public boolean isSlotTaken(int doctorId, LocalDate date, String time) {
        String sql = """
            SELECT COUNT(*) FROM appointments
            WHERE  doctor_id = ? AND appointment_date = ?
              AND  appointment_time = ? AND status != 'Cancelled'
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, doctorId);
            ps.setDate  (2, Date.valueOf(date));
            ps.setString(3, time);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("isSlotTaken error: " + e.getMessage());
        }
        return false;
    }

    // UPDATE - Re schedule/ change status
    public boolean updateAppointment(Appointment a) {
        String sql = """
            UPDATE appointments SET
                appointment_date = ?,
                appointment_time = ?,
                reason           = ?,
                status           = ?
            WHERE appointment_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate  (1, Date.valueOf(a.getDate()));
            ps.setTime  (2, Time.valueOf(a.getTime()));
            ps.setString(3, a.getReason());
            ps.setString(4, a.getStatus());
            ps.setInt   (5, a.getAppointmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateAppointment error: " + e.getMessage());
        }
        return false;
    }

    // Mark appointment as canceled
    public boolean cancelAppointment(int appointmentId) {
        String sql = "UPDATE appointments SET status='Cancelled' WHERE appointment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("cancelAppointment error: " + e.getMessage());
        }
        return false;
    }

    // Mark appointed as completed - cancel from doctors dashboard
    public boolean completeAppointment(int appointmentId) {
        String sql = "UPDATE appointments SET status='Completed' WHERE appointment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("completeAppointment error: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean deleteAppointment(int appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteAppointment error: " + e.getMessage());
        }
        return false;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorName(rs.getString("doctor_name"));
        Date d = rs.getDate("appointment_date");
        if (d != null) a.setDate(d.toLocalDate());
        Time t = rs.getTime("appointment_time");
        if (t != null) a.setTime(t.toLocalTime());
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        return a;
    }
}
