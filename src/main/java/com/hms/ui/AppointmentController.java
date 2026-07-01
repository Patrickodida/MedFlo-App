package com.hms.ui;

import com.hms.model.Appointment;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.service.AppointmentService;
import com.hms.service.PatientService;
import com.hms.dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentController implements Initializable {

    // ── Form fields ──────────────────────────────────────
    @FXML private ComboBox<String>  patientCombo;
    @FXML private ComboBox<String>  doctorCombo;
    @FXML private DatePicker        datePicker;
    @FXML private ComboBox<String>  timeCombo;
    @FXML private TextField         reasonField;
    @FXML private Label             statusLabel;

    private boolean isEditing = false;

    // ── Buttons ──────────────────────────────────────────
    @FXML private Button bookBtn;
    @FXML private Button rescheduleBtn;

    // ── Table ────────────────────────────────────────────
    @FXML private TableView<Appointment>              appointmentTable;
    @FXML private TableColumn<Appointment,Integer>    idCol;
    @FXML private TableColumn<Appointment,String>     patientCol;
    @FXML private TableColumn<Appointment,String>     doctorCol;
    @FXML private TableColumn<Appointment,String>     dateCol;
    @FXML private TableColumn<Appointment,String>     timeCol;
    @FXML private TableColumn<Appointment,String>     statusCol;

    private final AppointmentService appointmentService
            = new AppointmentService();
    private final PatientService patientService
            = new PatientService();
    private final UserDAO userDAO = new UserDAO();

    private List<Patient> patients;
    private List<User>    doctors;
    private Appointment   selectedAppointment = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadPatientCombo();
        loadDoctorCombo();
        loadTimeSlots();
        loadTable();
        rescheduleBtn.setVisible(false);

        // When a row is clicked load it into form for rescheduling
        appointmentTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null && !isEditing)
                        populateFormForEdit(selected);
                });
    }

    // ── Setup table columns ──────────────────────────────
    private void setupTable() {
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("appointmentId"));
        patientCol.setCellValueFactory(
                new PropertyValueFactory<>("patientName"));
        doctorCol.setCellValueFactory(
                new PropertyValueFactory<>("doctorName"));
        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date"));
        timeCol.setCellValueFactory(
                new PropertyValueFactory<>("time"));
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));
    }

    // ── Load patients into combo ─────────────────────────
    private void loadPatientCombo() {
        patients = patientService.getAllPatients();
        patientCombo.setItems(FXCollections.observableArrayList(
                patients.stream()
                        .map(p -> p.getPatientCode() + " - " + p.getFullName())
                        .toList()
        ));
    }

    // ── Load doctors into combo ──────────────────────────
    private void loadDoctorCombo() {
        doctors = userDAO.getAllUsers().stream()
                .filter(u -> u.getRole().equals("Doctor"))
                .toList();
        doctorCombo.setItems(FXCollections.observableArrayList(
                doctors.stream().map(User::getFullName).toList()
        ));
    }

    // ── Load available time slots ────────────────────────
    private void loadTimeSlots() {
        timeCombo.setItems(FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
        ));
    }

    // ════════════════════════════════
    // BOOK APPOINTMENT
    // ════════════════════════════════
    @FXML
    public void handleBook() {
        try {
            Appointment a = collectFormData();
            int id = appointmentService.bookAppointment(a);
            if (id > 0) {
                showStatus("Appointment booked successfully!", true);
                handleClear();
                loadTable();
            } else {
                showStatus("Failed to book appointment.", false);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            showStatus(e.getMessage(), false);
            System.err.println("Booking error: " + e.getMessage());
        } catch (Exception e) {
            showStatus("Unexpected error: " + e.getMessage(), false);
            System.err.println("Unexpected: " + e.getMessage());
        }
    }

    // ════════════════════════════════
    // RESCHEDULE
    // ════════════════════════════════
    @FXML
    public void handleReschedule() {
        if (selectedAppointment == null) {
            showStatus("Select an appointment to reschedule.", false);
            return;
        }
        if (datePicker.getValue() == null) {
            showStatus("Please select a new date.", false);
            return;
        }
        if (timeCombo.getValue() == null) {
            showStatus("Please select a new time slot.", false);
            return;
        }
        try {
            selectedAppointment.setDate(datePicker.getValue());
            selectedAppointment.setTime(
                    LocalTime.parse(timeCombo.getValue()));
            selectedAppointment.setReason(reasonField.getText().trim());
            selectedAppointment.setStatus("Scheduled");

            if (appointmentService.rescheduleAppointment(
                    selectedAppointment)) {
                showStatus("Appointment rescheduled successfully!", true);
                handleClear();
                loadTable();
            } else {
                showStatus("Reschedule failed. Try again.", false);
            }
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), false);
        }
    }

    // ════════════════════════════════
    // CANCEL
    // ════════════════════════════════
    @FXML
    public void handleCancel() {
        if (selectedAppointment == null) {
            showStatus("Select an appointment from the table to cancel.",
                    false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Appointment");
        confirm.setHeaderText("Cancel appointment for: "
                + selectedAppointment.getPatientName());
        confirm.setContentText(
                "Date: " + selectedAppointment.getDate() +
                        " at " + selectedAppointment.getTime() +
                        "\nThis cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (appointmentService.cancelAppointment(
                    selectedAppointment.getAppointmentId())) {
                showStatus("Appointment cancelled successfully.", true);
                handleClear();
                loadTable();
            } else {
                showStatus("Cancel failed. Try again.", false);
            }
        }
    }

    // ════════════════════════════════
    // CLEAR
    // ════════════════════════════════
    @FXML
    public void handleClear() {
        isEditing = false;

        patientCombo.setDisable(false);
        doctorCombo.setDisable(false);

        patientCombo.getSelectionModel().clearSelection();
        doctorCombo.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        timeCombo.getSelectionModel().clearSelection();
        reasonField.clear();
        statusLabel.setText("");
        selectedAppointment = null;
        bookBtn.setVisible(true);
        rescheduleBtn.setVisible(false);
        appointmentTable.getSelectionModel().clearSelection();
    }

    // ════════════════════════════════
    // BACK TO DASHBOARD
    // ════════════════════════════════
    @FXML
    public void handleBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/AdminDashboard.fxml"));
            Scene scene = new Scene(root, 1280, 780);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css")
                            .toExternalForm());
            Stage stage = (Stage) reasonField.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Back error: " + e.getMessage());
        }
    }

    // ════════════════════════════════
    // HELPERS
    // ════════════════════════════════
    private Appointment collectFormData() {
        Appointment a = new Appointment();

        int patientIndex = patientCombo.getSelectionModel()
                .getSelectedIndex();
        int doctorIndex  = doctorCombo.getSelectionModel()
                .getSelectedIndex();

        if (patientIndex >= 0)
            a.setPatientId(patients.get(patientIndex).getPatientId());
        if (doctorIndex >= 0) {
            int userId = doctors.get(doctorIndex).getUserId();
            int doctorId = getDoctorIdFromUserId(userId);
            a.setDoctorId(doctorId);
        }
        if (datePicker.getValue() != null)
            a.setDate(datePicker.getValue());
        if (timeCombo.getValue() != null)
            a.setTime(LocalTime.parse(timeCombo.getValue()));
        a.setReason(reasonField.getText().trim());
        return a;
    }

    private int getDoctorIdFromUserId(int userId) {
        String sql = "SELECT doctor_id FROM doctors WHERE user_id = ?";
        try (java.sql.Connection conn =
                     com.hms.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps =
                     conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("doctor_id");
        } catch (java.sql.SQLException e) {
            System.err.println("getDoctorId error: " + e.getMessage());
        }
        return -1;
    }

    private void populateFormForEdit(Appointment a) {
        isEditing = true;
        selectedAppointment = a;

        // Set date and time
        datePicker.setValue(a.getDate());
        if (a.getTime() != null)
            timeCombo.setValue(a.getTime().toString().substring(0, 5));

        // Set reason
        reasonField.setText(a.getReason() != null ? a.getReason() : "");

        // Match patient in combo by patientId
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId() == a.getPatientId()) {
                patientCombo.getSelectionModel().select(i);
                break;
            }
        }

        // Match doctor in combo
        for (int i = 0; i < doctors.size(); i++) {
            int docId = getDoctorIdFromUserId(
                    doctors.get(i).getUserId());
            if (docId == a.getDoctorId()) {
                doctorCombo.getSelectionModel().select(i);
                break;
            }
        }

        // Switch buttons — hide Book, show Reschedule
        bookBtn.setVisible(false);
        rescheduleBtn.setVisible(true);

        patientCombo.setDisable(true);
        doctorCombo.setDisable(true);

        isEditing = false;
    }

    private void loadTable() {
        appointmentTable.setItems(FXCollections.observableArrayList(
                appointmentService.getAllAppointments()));
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
                ? "-fx-text-fill:#2E7D32; -fx-font-weight:bold;"
                : "-fx-text-fill:#C62828; -fx-font-weight:bold;");
    }

}
