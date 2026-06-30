package com.hms.service;

import com.hms.dao.BillDAO;
import com.hms.model.Bill;
import java.util.ArrayList;
import java.util.List;

public class BillingService {

    private final BillDAO dao = new BillDAO();

    public String validateBill(Bill b) {
        if (b.getPatientId() <= 0)
            return "Please select a patient.";
        if (b.getTotalAmount() <= 0)
            return "Total amount must be greater than zero.";
        if (b.getDiscount() < 0)
            return "Discount cannot be negative.";
        if (b.getTax() < 0)
            return "Tax cannot be negative.";
        return null;
    }

    public int createBill(Bill b) {
        String error = validateBill(b);
        if (error != null) throw new IllegalArgumentException(error);
        b.recalculate();
        return dao.createBill(b);
    }

    public List<Bill> getAllBills() {
        return dao.getAllBills();
    }

    public List<Bill> getBillsByPatient(int patientId) {
        return dao.getBillsByPatient(patientId);
    }

    public double getTodaysRevenue() {
        return dao.getTodaysRevenue();
    }

    public boolean markAsPaid(int billId, String mode) {
        if (mode == null || mode.isBlank())
            throw new IllegalArgumentException("Payment mode is required.");
        return dao.markAsPaid(billId, mode);
    }

    public boolean updateBill(Bill b) {
        String error = validateBill(b);
        if (error != null) throw new IllegalArgumentException(error);
        b.recalculate();
        return dao.updateBill(b);
    }

    public boolean deleteBill(int billId) {
        return dao.deleteBill(billId);
    }
}
