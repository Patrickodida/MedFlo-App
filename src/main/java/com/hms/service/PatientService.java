package com.hms.service;

import com.hms.dao.PatientDAO;
import com.hms.model.Patient;
import com.hms.util.Validator;
import java.util.ArrayList;
import java.util.List;

public class PatientService {
    private final PatientDAO dao = new PatientDAO();

    public int registerPatient(Patient p) {
        String error = validatePatient(p);
        if (error != null) throw new IllegalArgumentException(error);
        p.setPatientCode(dao.generatePatientCode());
        return dao.registerPatient(p);
    }

    public List<Patient> getAllPatients() {
        return dao.getAllPatients();
    }

    public Patient getPatientById(int id) {
        return dao.getPatientById(id);
    }

    public List<Patient> searchPatients(String keyword) {
        return dao.searchPatients(keyword);
    }

    public boolean updatePatient(Patient p) {
        String error = validatePatient(p);
        if (error != null) throw new IllegalArgumentException(error);
        return dao.updatePatient(p);
    }

    public boolean deletePatient(int patientId) {
        return dao.deactivatePatient(patientId);
    }

    public String validatePatient(Patient p) {
        if (!Validator.isValidName(p.getFirstName()))
            return "First name is required (letters only, 2-50 characters).";
        if (!Validator.isValidName(p.getLastName()))
            return "Last name is required (letters only, 2-50 characters).";
        if (!Validator.isValidPhone(p.getPhone()))
            return "Phone number must be 10-13 digits.";
        if (!Validator.isValidEmail(p.getEmail()))
            return "Email address format is invalid.";
        if (p.getDateOfBirth() != null &&
                p.getDateOfBirth().isAfter(java.time.LocalDate.now()))
            return "Date of birth cannot be in the future.";
        return null;
    }

    public int getTotalCount() {
        return dao.getTotalPatientCount();
    }
}
