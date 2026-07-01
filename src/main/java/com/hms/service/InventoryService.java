package com.hms.service;

import com.hms.dao.InventoryDAO;
import com.hms.model.InventoryItem;
import com.hms.util.Validator;
import java.util.List;

public class InventoryService {

    private final InventoryDAO dao = new InventoryDAO();

    public String validateItem(InventoryItem item) {
        if (Validator.isEmpty(item.getItemName()))
            return "Item name is required.";
        if (item.getQuantity() < 0)
            return "Quantity cannot be negative.";
        if (item.getUnitPrice() < 0)
            return "Unit price cannot be negative.";
        return null;
    }

    public int addItem(InventoryItem item) {
        String error = validateItem(item);
        if (error != null) throw new IllegalArgumentException(error);
        return dao.addItem(item);
    }

    public List<InventoryItem> getAllItems() {
        return dao.getAllItems();
    }

    public List<InventoryItem> getLowStockItems() {
        return dao.getLowStockItems();
    }

    public boolean updateItem(InventoryItem item) {
        String error = validateItem(item);
        if (error != null) throw new IllegalArgumentException(error);
        return dao.updateItem(item);
    }

    public boolean deleteItem(int itemId) {
        return dao.deleteItem(itemId);
    }

    public boolean adjustQuantity(int itemId, int newQuantity) {
        if (newQuantity < 0)
            throw new IllegalArgumentException(
                    "Quantity cannot be negative.");
        return dao.adjustQuantity(itemId, newQuantity);
    }
}
