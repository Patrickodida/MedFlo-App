package com.hms.ui;

import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.service.BillingService;
import com.hms.service.AppointmentService;
import com.hms.service.PatientService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable, DashboardController {

    // ── Top bar ──────────────────────────────────────────
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;

    // ── Stat cards ───────────────────────────────────────
    @FXML private Label totalPatientsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label todayRevenueLabel;

    // ── Recent patients table ────────────────────────────
    @FXML private TableView<Patient>           patientTable;
    @FXML private TableColumn<Patient,Integer> idCol;
    @FXML private TableColumn<Patient,String>  codeCol;
    @FXML private TableColumn<Patient,String>  nameCol;
    @FXML private TableColumn<Patient,String>  phoneCol;
    @FXML private TableColumn<Patient,String>  genderCol;
    @FXML private TableColumn<Patient,Integer> ageCol;

    private final PatientService     patientService     = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final BillingService     billingService     = new BillingService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadStats();
        loadRecentPatients();
        dateLabel.setText(java.time.LocalDate.now().toString());
    }

    @Override
    public void setUser(User user) {
        welcomeLabel.setText("Welcome, " + user.getFullName());
    }

    // ── Setup table columns ──────────────────────────────
    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        codeCol.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
    }

    // ── Load stat card numbers ───────────────────────────
    private void loadStats() {
        totalPatientsLabel.setText(
                String.valueOf(patientService.getTotalCount()));
        todayAppointmentsLabel.setText(
                String.valueOf(appointmentService.getTodaysCount()));
        todayRevenueLabel.setText(
                String.format("UGX %.0f", billingService.getTodaysRevenue()));
    }

    // ── Load recent patients into table ──────────────────
    private void loadRecentPatients() {
        List<Patient> patients = patientService.getAllPatients();
        patientTable.setItems(FXCollections.observableArrayList(patients));
    }

    // ── Sidebar navigation ───────────────────────────────
    @FXML
    public void goToPatients() {
        loadScreen("/fxml/PatientRegistration.fxml");
    }

    @FXML
    public void goToAppointments() {
        loadScreen("/fxml/AppointmentScheduler.fxml");
    }

    @FXML
    public void goToBilling() {
        loadScreen("/fxml/Billing.fxml");
    }

    @FXML
    public void goToPharmacy() {
        loadScreen("/fxml/Pharmacy.fxml");
    }

    @FXML
    public void handleLogout() {
        try {
            com.hms.service.AuthService.logout();
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Medicare Hospital — Login");
        } catch (IOException e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    // ── Helper to navigate to any screen ─────────────────
    private void loadScreen(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath));
            Scene scene = new Scene(root, 1280, 780);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Could not load " + fxmlPath +
                    " — " + e.getMessage());
        }
    }
}
