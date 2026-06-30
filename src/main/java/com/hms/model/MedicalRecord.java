package com.hms.model;

import java.time.LocalDate;

public class MedicalRecord {
    private int recordId;
    private int patientId;
    private int doctorId;
    private String patientName;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private String notes;
    private LocalDate recordDate;

    public MedicalRecord() {this.recordDate = LocalDate.now();}

    public int getRecordId() {return recordId;}
    public int getPatientId() {return patientId;}
    public int getDoctorId() {return doctorId;}
    public String getPatientName() {return patientName;}
    public String getDoctorName() {return doctorName;}
    public String getDiagnosis() {return diagnosis;}
    public String getTreatment() {return treatment;}
    public String getNotes() {return notes;}
    public LocalDate getRecordDate() {return recordDate;}

    public void setRecordId(int v) {this.recordId = v;}
    public void setPatientId(int v) {this.patientId = v;}
    public void setDoctorId(int v) {this.doctorId = v;}
    public void setPatientName(String v) {this.patientName = v;}
    public void setDoctorName(String v) {this.doctorName = v;}
    public void setDiagnosis(String v) {this.diagnosis = v;}
    public void setTreatment(String v) {this.treatment = v;}
    public void setNotes(String v) {this.notes = v;}
    public void setRecordDate(LocalDate v) {this.recordDate = v;}
}
