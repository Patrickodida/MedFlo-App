package com.hms.service;

import com.hms.dao.AppointmentDAO;
import com.hms.model.Appointment;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private final AppointmentDAO dao = new AppointmentDAO();

    public String validateAppointment(Appointment a) {
        if (a.getPatientId() <= 0)
            return "Please select a patient.";
        if (a.getDoctorId() <= 0)
            return "Please select a doctor.";
        if (a.getDate() == null)
            return "Appointment date is required.";
        if (a.getDate().isBefore(LocalDate.now()))
            return "Appointment date cannot be in the past.";
        if (a.getTime() == null)
            return "Appointment time is required.";
        return null;
    }

    public int bookAppointment(Appointment a) {
        String error = validateAppointment(a);
        if (error != null) throw new IllegalArgumentException(error);
        if (dao.isSlotTaken(a.getDoctorId(), a.getDate(), a.getTime().toString()))
            throw new IllegalStateException(
                    "That time slot is already booked. Please choose another.");
        return dao.bookAppointment(a);
    }

    public List<Appointment> getAllAppointments() {
        return dao.getAllAppointments();
    }

    public List<Appointment> getTodaysAppointments(int doctorId) {
        return dao.getTodaysAppointments(doctorId);
    }

    public int getTodaysCount() {
        return dao.getTodaysAppointmentCount();
    }

    public boolean rescheduleAppointment(Appointment a) {
        String error = validateAppointment(a);
        if (error != null) throw new IllegalArgumentException(error);
        return dao.updateAppointment(a);
    }

    public boolean cancelAppointment(int id) {
        return dao.cancelAppointment(id);
    }

    public boolean completeAppointment(int id) {
        return dao.completeAppointment(id);
    }
}
