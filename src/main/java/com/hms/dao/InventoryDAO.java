package com.hms.dao;
import com.hms.model.InventoryItem;
import com.hms.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    // CREATE
    public int addItem(InventoryItem item) {
        String sql = """
            INSERT INTO inventory
              (item_name, category, quantity, unit, unit_price,
               expiry_date, supplier_name, reorder_level, last_updated)
            VALUES (?,?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt   (3, item.getQuantity());
            ps.setString(4, item.getUnit());
            ps.setDouble(5, item.getUnitPrice());
            ps.setDate  (6, item.getExpiryDate() != null ? Date.valueOf(item.getExpiryDate()) : null);
            ps.setString(7, item.getSupplierName());
            ps.setInt   (8, item.getReorderLevel());
            ps.setDate  (9, Date.valueOf(item.getLastUpdated()));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("addItem error: " + e.getMessage());
        }
        return -1;
    }

    // READ - ALL
    public List<InventoryItem> getAllItems() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory ORDER BY item_name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAllItems error: " + e.getMessage());
        }
        return list;
    }

    // READ - Item by Id
    public InventoryItem getItemById(int itemId) {
        String sql = "SELECT * FROM inventory WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("getItemById error: " + e.getMessage());
        }
        return null;
    }

    // READ — low stock items only (for alerts)
    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity <= reorder_level ORDER BY quantity";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getLowStockItems error: " + e.getMessage());
        }
        return list;
    }

    // UPDATE - update details
    public boolean updateItem(InventoryItem item) {
        String sql = """
            UPDATE inventory SET
                item_name     = ?,
                category      = ?,
                quantity      = ?,
                unit          = ?,
                unit_price    = ?,
                expiry_date   = ?,
                supplier_name = ?,
                reorder_level = ?,
                last_updated  = ?
            WHERE item_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt   (3, item.getQuantity());
            ps.setString(4, item.getUnit());
            ps.setDouble(5, item.getUnitPrice());
            ps.setDate  (6, item.getExpiryDate() != null ? Date.valueOf(item.getExpiryDate()) : null);
            ps.setString(7, item.getSupplierName());
            ps.setInt   (8, item.getReorderLevel());
            ps.setDate  (9, Date.valueOf(item.getLastUpdated()));
            ps.setInt   (10, item.getItemId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateItem error: " + e.getMessage());
        }
        return false;
    }

    // UPDATE - Adjust quantity only
    public boolean adjustQuantity(int itemId, int newQuantity) {
        String sql = "UPDATE inventory SET quantity=?, last_updated=? WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt (1, newQuantity);
            ps.setDate(2, Date.valueOf(java.time.LocalDate.now()));
            ps.setInt (3, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("adjustQuantity error: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM inventory WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteItem error: " + e.getMessage());
        }
        return false;
    }

    private InventoryItem mapRow(ResultSet rs) throws SQLException {
        InventoryItem i = new InventoryItem();
        i.setItemId(rs.getInt("item_id"));
        i.setItemName(rs.getString("item_name"));
        i.setCategory(rs.getString("category"));
        i.setQuantity(rs.getInt("quantity"));
        i.setUnit(rs.getString("unit"));
        i.setUnitPrice(rs.getDouble("unit_price"));
        Date exp = rs.getDate("expiry_date");
        if (exp != null) i.setExpiryDate(exp.toLocalDate());
        i.setSupplierName(rs.getString("supplier_name"));
        i.setReorderLevel(rs.getInt("reorder_level"));
        Date lu = rs.getDate("last_updated");
        if (lu != null) i.setLastUpdated(lu.toLocalDate());
        return i;
    }
}
