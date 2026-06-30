package com.hms.model;

import java.time.LocalDate;

public class Bill {
    private int billId;
    private int patientId;
    private String patientName;
    private double totalAmount;
    private double discount;
    private double tax;
    private double finalAmount;
    private String paymentStatus;
    private String paymentMode;
    private LocalDate billDate;
    private String notes;

    public Bill() {
        this.billDate = LocalDate.now();
        this.paymentStatus = "Unpaid";
        this.discount = 0;
        this.tax = 0;
    }

    public void recalculate() {
        this.finalAmount = totalAmount - discount + tax;
    }

    public int getBillId() {return billId;}
    public int getPatientId() {return patientId;}
    public String getPatientName() {return patientName;}
    public double getTotalAmount() {return totalAmount;}
    public double getDiscount() {return discount;}
    public double getTax() {return tax;}
    public double getFinalAmount() {return finalAmount;}
    public String getPaymentStatus() {return paymentStatus;}
    public String getPaymentMode() {return paymentMode;}
    public LocalDate getBillDate() {return billDate;}
    public String getNotes() {return notes;}

    public void setBillId(int v) {this.billId = v;}
    public void setPatientId(int v) {this.patientId = v;}
    public void setPatientName(String v) {this.patientName = v;}
    public void setTotalAmount(double v) {this.totalAmount = v;}
    public void setDiscount(double v) {this.discount = v;}
    public void setTax(double v) {this.tax = v;}
    public void setFinalAmount(double v) {this.finalAmount = v;}
    public void setPaymentStatus(String v) {this.paymentStatus = v;}
    public void setPaymentMode(String v) {this.paymentMode = v;}
    public void setBillDate(LocalDate v) {this.billDate = v;}
    public void setNotes(String v) {this.notes = v;}
}
