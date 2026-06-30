package com.hms.service;

import com.hms.model.Patient;
import java.util.ArrayList;
import java.util.List;

public class PatientService {
    public int registerPatient(Patient p) {
        return -1;
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>();
    }

    public Patient getPatientById(int id) {
        return null;
    }

    public List<Patient> searchPatients(String keyword) {
        return new ArrayList<>();
    }

    public boolean updatePatient(Patient p) {
        return false;
    }

    public boolean deletePatient(int id) {
        return false;
    }

    public String validatePatient(Patient p) {
        return null;
    }

    public int getTotalCount() {
        return 0;
    }
}
