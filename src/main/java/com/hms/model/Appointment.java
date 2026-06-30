package com.hms.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private String patientName;
    private String doctorName;
    private LocalDate date;
    private LocalTime time;
    private String reason;
    private String status;

    public Appointment() {this.status = "Scheduled";}

    public int getAppointmentId() {return this.appointmentId;}
    public int getPatientId() {return this.patientId;}
    public int getDoctorId() {return this.doctorId;}
    public String getPatientName() {return this.patientName;}
    public String getDoctorName() {return this.doctorName;}
    public LocalDate getDate() {return this.date;}
    public LocalTime getTime() {return this.time;}
    public String getReason() {return this.reason;}
    public String getStatus() {return this.status;}

    public void setAppointmentId(int v) {this.appointmentId = v;}
    public void setPatientId(int v) {this.patientId = v;}
    public void setDoctorId(int v) {this.doctorId = v;}
    public void setPatientName(String v) {this.patientName = v;}
    public void setDoctorName(String v) {this.doctorName = v;}
    public void setDate(LocalDate v) {this.date = v;}
    public void setTime(LocalTime v) {this.time = v;}
    public void setReason(String v) {this.reason = v;}
    public void setStatus(String v) {this.status = v;}
}
