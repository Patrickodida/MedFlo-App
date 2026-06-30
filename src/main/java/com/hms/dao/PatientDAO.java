package com.hms.dao;
import com.hms.model.Patient;
import com.hms.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    // CREATE
    public int registerPatient(Patient p) {
        String sql = """
                INSERT INTO patients
                      (patient_code, first_name, last_name, date_of_birth,
                      gender, blood_group, phone, email, address,
                      emergency_contact, emergency_phone, medical_history,
                      registered_on, is_active)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,1)
                """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPatientCode());
            ps.setString(2, p.getFirstName());
            ps.setString(3, p.getLastName());
            ps.setDate  (4, p.getDateOfBirth() != null ? Date.valueOf(p.getDateOfBirth()) : null);
            ps.setString(5, p.getGender());
            ps.setString(6, p.getBloodGroup());
            ps.setString(7, p.getPhone());
            ps.setString(8, p.getEmail());
            ps.setString(9, p.getAddress());
            ps.setString(10, p.getEmergencyContact());
            ps.setString(11, p.getEmergencyPhone());
            ps.setString(12, p.getMedicalHistory());
            ps.setDate  (13, Date.valueOf(p.getRegisteredOn()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("registeredPatient Error: " + e.getMessage());
        }
        return 1;
    }

    // READ - All active patients (newest first)
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE is_active=1 ORDER BY registered_on DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAllPatients error: " + e.getMessage());
        }
        return list;
    }

    // Single patient by primary key
    public Patient getPatientById(int patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("getPatientById error: " + e.getMessage());
        }
        return null;
    }

    // Search by name, phone, or patientCode
    public List<Patient> searchPatients(String keyword) {
        List<Patient> list = new ArrayList<>();
        String sql = """
            SELECT * FROM patients
            WHERE  is_active = 1
              AND  (first_name   LIKE ? OR last_name    LIKE ?
                OR  phone        LIKE ? OR patient_code LIKE ?)
            ORDER  BY first_name
            """;
        String like = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("searchPatients error: " + e.getMessage());
        }
        return list;
    }

    // Count of all active patients
    public int getTotalPatientCount() {
        String sql = "SELECT COUNT(*) FROM patients WHERE is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("getTotalPatientCount error: " + e.getMessage());
        }
        return 0;
    }


    // UPDATE - Edit Patients details
    public boolean updatePatient(Patient p) {
        String sql = """
            UPDATE patients SET
                first_name        = ?,
                last_name         = ?,
                date_of_birth     = ?,
                gender            = ?,
                blood_group       = ?,
                phone             = ?,
                email             = ?,
                address           = ?,
                emergency_contact = ?,
                emergency_phone   = ?,
                medical_history   = ?
            WHERE patient_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  p.getFirstName());
            ps.setString(2,  p.getLastName());
            ps.setDate  (3,  p.getDateOfBirth() != null ? Date.valueOf(p.getDateOfBirth()) : null);
            ps.setString(4,  p.getGender());
            ps.setString(5,  p.getBloodGroup());
            ps.setString(6,  p.getPhone());
            ps.setString(7,  p.getEmail());
            ps.setString(8,  p.getAddress());
            ps.setString(9,  p.getEmergencyContact());
            ps.setString(10, p.getEmergencyPhone());
            ps.setString(11, p.getMedicalHistory());
            ps.setInt   (12, p.getPatientId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updatePatient error: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean deactivatePatient(int patientId) {
        String sql = "UPDATE patients SET is_active = 0 WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deactivatePatient error: " + e.getMessage());
        }
        return false;
    }

    // HELPER - Generate next Patients Code
    public String generatePatientCode() {
        String sql = "SELECT ISNULL(MAX(patient_id),0)+1 AS next_id FROM patients";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return String.format("MH-%05d", rs.getInt("next_id"));
        } catch (SQLException e) {
            System.err.println("generatePatientCode error: " + e.getMessage());
        }
        return "MH-00001";
    }

    // HELPER — Map one ResultSet row to a Patient object
    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) p.setDateOfBirth(dob.toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setEmergencyPhone(rs.getString("emergency_phone"));
        p.setMedicalHistory(rs.getString("medical_history"));
        Date reg = rs.getDate("registered_on");
        if (reg != null) p.setRegisteredOn(reg.toLocalDate());
        p.setActive(rs.getBoolean("is_active"));
        return p;
    }
}
