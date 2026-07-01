package com.hms.ui;

import com.hms.dao.MedicalRecordDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Appointment;
import com.hms.model.MedicalRecord;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.service.AppointmentService;
import com.hms.dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorDashboardController
        implements Initializable, DashboardController {

    // ── Top bar ──────────────────────────────────────────
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;

    // ── Today's appointments table ───────────────────────
    @FXML private TableView<Appointment>           appointmentTable;
    @FXML private TableColumn<Appointment,String>  patientCol;
    @FXML private TableColumn<Appointment,String>  timeCol;
    @FXML private TableColumn<Appointment,String>  reasonCol;
    @FXML private TableColumn<Appointment,String>  statusCol;

    // ── EMR Panel ────────────────────────────────────────
    @FXML private VBox       emrPanel;
    @FXML private Label      emrPatientLabel;
    @FXML private Label      emrPatientInfo;
    @FXML private TextArea   diagnosisArea;
    @FXML private TextArea   treatmentArea;
    @FXML private TextArea   notesArea;
    @FXML private Label      emrStatusLabel;

    // ── Medical history table ────────────────────────────
    @FXML private TableView<MedicalRecord>          historyTable;
    @FXML private TableColumn<MedicalRecord,String> histDateCol;
    @FXML private TableColumn<MedicalRecord,String> histDiagCol;
    @FXML private TableColumn<MedicalRecord,String> histTreatCol;

    private final AppointmentService appointmentService
            = new AppointmentService();
    private final MedicalRecordDAO   recordDAO
            = new MedicalRecordDAO();
    private final PatientDAO         patientDAO
            = new PatientDAO();
    private final UserDAO            userDAO
            = new UserDAO();

    private User        currentUser;
    private int         currentDoctorId = -1;
    private Appointment selectedAppointment;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupAppointmentTable();
        setupHistoryTable();
        dateLabel.setText(java.time.LocalDate.now().toString());
        emrPanel.setVisible(false);
    }

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText(
                "Welcome, " + user.getFullName());

        // Get the doctor_id from users table
        currentDoctorId = getDoctorIdFromUserId(user.getUserId());
        loadTodaysAppointments();
    }

    // ── Setup appointment table columns ──────────────────
    private void setupAppointmentTable() {
        patientCol.setCellValueFactory(
                new PropertyValueFactory<>("patientName"));
        timeCol.setCellValueFactory(
                new PropertyValueFactory<>("time"));
        reasonCol.setCellValueFactory(
                new PropertyValueFactory<>("reason"));
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        // When appointment row is clicked open EMR
        appointmentTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null)
                        loadEMR(selected);
                });
    }

    // ── Setup history table columns ───────────────────────
    private void setupHistoryTable() {
        histDateCol.setCellValueFactory(
                new PropertyValueFactory<>("recordDate"));
        histDiagCol.setCellValueFactory(
                new PropertyValueFactory<>("diagnosis"));
        histTreatCol.setCellValueFactory(
                new PropertyValueFactory<>("treatment"));
    }

    // ── Load today's appointments ────────────────────────
    private void loadTodaysAppointments() {
        if (currentDoctorId == -1) return;
        List<Appointment> appts = appointmentService
                .getTodaysAppointments(currentDoctorId);
        appointmentTable.setItems(
                FXCollections.observableArrayList(appts));
    }

    // ── Load EMR when appointment is selected ────────────
    private void loadEMR(Appointment appt) {
        selectedAppointment = appt;
        emrPanel.setVisible(true);

        // Load patient details
        Patient patient = patientDAO
                .getPatientById(appt.getPatientId());
        if (patient != null) {
            emrPatientLabel.setText(patient.getFullName());
            emrPatientInfo.setText(
                    "Age: " + patient.getAge()
                            + "  |  Gender: " + patient.getGender()
                            + "  |  Blood Group: " + patient.getBloodGroup()
                            + "  |  Phone: " + patient.getPhone());
        }

        // Clear previous entry
        diagnosisArea.clear();
        treatmentArea.clear();
        notesArea.clear();
        emrStatusLabel.setText("");

        // Load medical history
        List<MedicalRecord> history = recordDAO
                .getRecordsByPatient(appt.getPatientId());
        historyTable.setItems(
                FXCollections.observableArrayList(history));
    }

    // ════════════════════════════════
    // SAVE EMR RECORD
    // ════════════════════════════════
    @FXML
    public void handleSaveEMR() {
        if (selectedAppointment == null) {
            showEMRStatus("Select an appointment first.", false);
            return;
        }
        if (diagnosisArea.getText().isBlank()) {
            showEMRStatus("Diagnosis is required.", false);
            return;
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(selectedAppointment.getPatientId());
        record.setDoctorId(currentDoctorId);
        record.setDiagnosis(diagnosisArea.getText().trim());
        record.setTreatment(treatmentArea.getText().trim());
        record.setNotes(notesArea.getText().trim());
        record.setRecordDate(java.time.LocalDate.now());

        int id = recordDAO.createRecord(record);
        if (id > 0) {
            showEMRStatus("EMR record saved successfully!", true);

            // Mark appointment as completed
            appointmentService.completeAppointment(
                    selectedAppointment.getAppointmentId());

            // Refresh history table
            List<MedicalRecord> history = recordDAO
                    .getRecordsByPatient(
                            selectedAppointment.getPatientId());
            historyTable.setItems(
                    FXCollections.observableArrayList(history));

            // Refresh appointments list
            loadTodaysAppointments();

            // Clear form
            diagnosisArea.clear();
            treatmentArea.clear();
            notesArea.clear();
        } else {
            showEMRStatus("Failed to save EMR. Try again.", false);
        }
    }

    // ════════════════════════════════
    // LOGOUT
    // ════════════════════════════════
    @FXML
    public void handleLogout() {
        try {
            com.hms.service.AuthService.logout();
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass().getResource("/fxml/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene =
                    new javafx.scene.Scene(root, 1000, 650);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css")
                            .toExternalForm());
            javafx.stage.Stage stage =
                    (javafx.stage.Stage) welcomeLabel
                            .getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Medicare Hospital — Login");
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    // ════════════════════════════════
    // HELPERS
    // ════════════════════════════════
    private int getDoctorIdFromUserId(int userId) {
        String sql = "SELECT doctor_id FROM doctors WHERE user_id=?";
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

    private void showEMRStatus(String msg, boolean success) {
        emrStatusLabel.setText(msg);
        emrStatusLabel.setStyle(success
                ? "-fx-text-fill:#2E7D32; -fx-font-weight:bold;"
                : "-fx-text-fill:#C62828; -fx-font-weight:bold;");
    }
}