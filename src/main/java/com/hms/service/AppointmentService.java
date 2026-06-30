package com.hms.service;

import com.hms.model.Appointment;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    public int bookAppointment(Appointment a) {
        return -1;
    }

    public List<Appointment> getAllAppointments() {
        return new ArrayList<>();
    }

    public List<Appointment> getTodaysAppointments(int doctorId) {
        return new ArrayList<>();
    }

    public int getTodaysCount() {
        return 0;
    }

    public boolean rescheduleAppointment(Appointment a) {
        return false;
    }

    public boolean cancelAppointment(int id) {
        return false;
    }

    public boolean completeAppointment(int id) {
        return false;
    }

    public String validateAppointment(Appointment a) {
        return null;
    }
}
