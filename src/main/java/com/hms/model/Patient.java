package com.hms.model;

import java.time.LocalDate;
import java.time.Period;

public class Patient {
    private int patientId;
    private String patientCode;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String email;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String medicalHistory;
    private LocalDate registeredOn;
    private boolean active;

    public Patient() {
        this.registeredOn = LocalDate.now();
        this.active = true;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public int getAge() {
        if(dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public int getPatientId() {return patientId;}
    public String getPatientCode() {return patientCode;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public LocalDate getDateOfBirth() {return dateOfBirth;}
    public String getGender() {return gender;}
    public String getBloodGroup() {return bloodGroup;}
    public String getPhone() {return phone;}
    public String getEmail() {return email;}
    public String getAddress() {return address;}
    public String getEmergencyContact() {return emergencyContact;}
    public String getEmergencyPhone() {return emergencyPhone;}
    public String getMedicalHistory() {return medicalHistory;}
    public LocalDate getRegisteredOn() {return registeredOn;}
    public boolean isActive() {return active;}

    public void setPatientId(int v) {this.patientId = v;}
    public void setPatientCode(String v) {this.patientCode = v;}
    public void setFirstName(String v) {this.firstName = v;}
    public void setLastName(String v) {this.lastName = v;}
    public void setDateOfBirth(LocalDate v) {this.dateOfBirth = v;}
    public void setGender(String v) {this.gender = v;}
    public void setBloodGroup(String v) {this.bloodGroup = v;}
    public void setPhone(String v) {this.phone = v;}
    public void setEmail(String v) {this.email = v;}
    public void setAddress(String v) {this.address = v;}
    public void setEmergencyContact(String v) {this.emergencyContact = v;}
    public void setEmergencyPhone(String v) {this.emergencyPhone = v;}
    public void setMedicalHistory(String v) {this.medicalHistory = v;}
    public void setRegisteredOn(LocalDate v) {this.registeredOn = v;}
    public void setActive(boolean v) {this.active = v;}
}
