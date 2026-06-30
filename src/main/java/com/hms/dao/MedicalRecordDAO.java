package com.hms.dao;
import com.hms.model.MedicalRecord;
import com.hms.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {
    // CREATE
    public int createRecord(MedicalRecord r) {
        String sql = """
            INSERT INTO medical_records
              (patient_id, doctor_id, diagnosis, treatment, notes, record_date)
            VALUES (?,?,?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, r.getPatientId());
            ps.setInt   (2, r.getDoctorId());
            ps.setString(3, r.getDiagnosis());
            ps.setString(4, r.getTreatment());
            ps.setString(5, r.getNotes());
            ps.setDate  (6, Date.valueOf(r.getRecordDate()));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("createRecord error: " + e.getMessage());
        }
        return -1;
    }

    // READ - All records for a patient
    public List<MedicalRecord> getRecordsByPatient(int patientId) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = """
            SELECT mr.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name,
                   u.full_name AS doctor_name
            FROM   medical_records mr
            JOIN   patients p ON mr.patient_id = p.patient_id
            JOIN   doctors  d ON mr.doctor_id  = d.doctor_id
            JOIN   users    u ON d.user_id     = u.user_id
            WHERE  mr.patient_id = ?
            ORDER  BY mr.record_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getRecordsByPatient error: " + e.getMessage());
        }
        return list;
    }

    // READ - ONE
    public MedicalRecord getRecordById(int recordId) {
        String sql = """
            SELECT mr.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name,
                   u.full_name AS doctor_name
            FROM   medical_records mr
            JOIN   patients p ON mr.patient_id = p.patient_id
            JOIN   doctors  d ON mr.doctor_id  = d.doctor_id
            JOIN   users    u ON d.user_id     = u.user_id
            WHERE  mr.record_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("getRecordById error: " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean updateRecord(MedicalRecord r) {
        String sql = """
            UPDATE medical_records
            SET diagnosis = ?, treatment = ?, notes = ?
            WHERE record_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getDiagnosis());
            ps.setString(2, r.getTreatment());
            ps.setString(3, r.getNotes());
            ps.setInt   (4, r.getRecordId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateRecord error: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean deleteRecord(int recordId) {
        String sql = "DELETE FROM medical_records WHERE record_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteRecord error: " + e.getMessage());
        }
        return false;
    }

    private MedicalRecord mapRow(ResultSet rs) throws SQLException {
        MedicalRecord r = new MedicalRecord();
        r.setRecordId(rs.getInt("record_id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setPatientName(rs.getString("patient_name"));
        r.setDoctorName(rs.getString("doctor_name"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setTreatment(rs.getString("treatment"));
        r.setNotes(rs.getString("notes"));
        Date d = rs.getDate("record_date");
        if (d != null) r.setRecordDate(d.toLocalDate());
        return r;
    }
}
