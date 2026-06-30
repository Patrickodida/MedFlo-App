package com.hms.dao;
import com.hms.model.Bill;
import com.hms.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    // CREATE
    public int createBill(Bill b) {
        String sql = """
            INSERT INTO bills
              (patient_id, total_amount, discount, tax,
               final_amount, payment_status, bill_date, notes)
            VALUES (?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, b.getPatientId());
            ps.setDouble(2, b.getTotalAmount());
            ps.setDouble(3, b.getDiscount());
            ps.setDouble(4, b.getTax());
            ps.setDouble(5, b.getFinalAmount());
            ps.setString(6, b.getPaymentStatus());
            ps.setDate  (7, Date.valueOf(b.getBillDate()));
            ps.setString(8, b.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("createBill error: " + e.getMessage());
        }
        return -1;
    }

    // READ - All Bills
    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = """
            SELECT b.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name
            FROM   bills b
            JOIN   patients p ON b.patient_id = p.patient_id
            ORDER  BY b.bill_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAllBills error: " + e.getMessage());
        }
        return list;
    }

    // READ - Bill for one patient
    public List<Bill> getBillsByPatient(int patientId) {
        List<Bill> list = new ArrayList<>();
        String sql = """
            SELECT b.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name
            FROM   bills b
            JOIN   patients p ON b.patient_id = p.patient_id
            WHERE  b.patient_id = ?
            ORDER  BY b.bill_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getBillsByPatient error: " + e.getMessage());
        }
        return list;
    }

    // READ - Today's revenue for Admin Dashboard
    public double getTodaysRevenue() {
        String sql = """
            SELECT ISNULL(SUM(final_amount),0)
            FROM   bills
            WHERE  payment_status='Paid'
              AND  bill_date = CAST(GETDATE() AS DATE)
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("getTodaysRevenue error: " + e.getMessage());
        }
        return 0;
    }

    // UPDATE - Mark as Paid
    public boolean markAsPaid(int billId, String paymentMode) {
        String sql = """
            UPDATE bills
            SET payment_status='Paid', payment_mode=?
            WHERE bill_id=?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Note: payment_mode column — add it to bills table or use payments table
            ps.setString(1, paymentMode);
            ps.setInt   (2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("markAsPaid error: " + e.getMessage());
        }
        return false;
    }

    // UPDATE - Update Bill Amount
    public boolean updateBill(Bill b) {
        String sql = """
            UPDATE bills SET
                total_amount   = ?,
                discount       = ?,
                tax            = ?,
                final_amount   = ?,
                payment_status = ?,
                notes          = ?
            WHERE bill_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, b.getTotalAmount());
            ps.setDouble(2, b.getDiscount());
            ps.setDouble(3, b.getTax());
            ps.setDouble(4, b.getFinalAmount());
            ps.setString(5, b.getPaymentStatus());
            ps.setString(6, b.getNotes());
            ps.setInt   (7, b.getBillId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateBill error: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean deleteBill(int billId) {
        String sql = "DELETE FROM bills WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteBill error: " + e.getMessage());
        }
        return false;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setPatientId(rs.getInt("patient_id"));
        b.setPatientName(rs.getString("patient_name"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setDiscount(rs.getDouble("discount"));
        b.setTax(rs.getDouble("tax"));
        b.setFinalAmount(rs.getDouble("final_amount"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setNotes(rs.getString("notes"));
        Date d = rs.getDate("bill_date");
        if (d != null) b.setBillDate(d.toLocalDate());
        return b;
    }
}
