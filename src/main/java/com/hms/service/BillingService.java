package com.hms.service;

import com.hms.model.Bill;
import java.util.ArrayList;
import java.util.List;

public class BillingService {
    public int createBill(Bill b) {
        return -1;
    }

    public List<Bill> getAllBills() {
        return new ArrayList<>();
    }

    public List<Bill> getBillsByPatient(int patientId) {
        return new ArrayList<>();
    }

    public double getTodaysRevenue() {
        return 0;
    }

    public boolean markAsPaid(int billId, String mode) {
        return false;
    }

    public boolean updateBill(Bill b) {
        return false;
    }

    public boolean deleteBill(int billId) {
        return false;
    }

    public String validateBill(Bill b) {
        return null;
    }
}
