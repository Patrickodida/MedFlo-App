package com.hms.model;

import java.time.LocalDate;

public class InventoryItem {
    private int itemId;
    private String itemName;
    private String category;
    private int quantity;
    private String unit;
    private double unitPrice;
    private LocalDate expiryDate;
    private String supplierName;
    private int reorderLevel;
    private LocalDate lastUpdated;

    public InventoryItem() {
        this.lastUpdated = LocalDate.now();
        this.reorderLevel = 10;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    public boolean isNearExpiry() {
        if (expiryDate == null) {return false;}
        return !expiryDate.isAfter(LocalDate.now().plusDays(30));
    }

    public int getItemId() {return itemId;}
    public String getItemName() {return itemName;}
    public String getCategory() {return category;}
    public int getQuantity() {return quantity;}
    public String getUnit() {return unit;}
    public double getUnitPrice() {return unitPrice;}
    public LocalDate getExpiryDate() {return expiryDate;}
    public String getSupplierName() {return supplierName;}
    public int getReorderLevel() {return reorderLevel;}
    public LocalDate getLastUpdated() {return lastUpdated;}

    public void setItemId(int v) {this.itemId = v;}
    public void setItemName(String v) {this.itemName = v;}
    public void setCategory(String v) {this.category = v;}
    public void setQuantity(int v) {this.quantity = v;}
    public void setUnit(String v) {this.unit = v;}
    public void setUnitPrice(double v) {this.unitPrice = v;}
    public void setExpiryDate(LocalDate v) {this.expiryDate = v;}
    public void setSupplierName(String v) {this.supplierName = v;}
    public void setReorderLevel(int v) {this.reorderLevel = v;}
    public void setLastUpdated(LocalDate v) {this.lastUpdated = v;}
}
