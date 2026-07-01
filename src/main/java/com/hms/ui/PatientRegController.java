package com.hms.ui;

import com.hms.model.Patient;
import com.hms.service.PatientService;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PatientRegController implements Initializable {

    // ── Form fields ──────────────────────────────────────
    @FXML private TextField   firstNameField;
    @FXML private TextField   lastNameField;
    @FXML private DatePicker  dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField   phoneField;
    @FXML private TextField   emailField;
    @FXML private TextArea    addressArea;
    @FXML private TextField   emergencyContactField;
    @FXML private TextField   emergencyPhoneField;
    @FXML private TextArea    medicalHistoryArea;
    @FXML private Label       patientCodeLabel;
    @FXML private Label       statusLabel;

    // ── Buttons ──────────────────────────────────────────
    @FXML private Button saveBtn;
    @FXML private Button updateBtn;

    // ── Table ────────────────────────────────────────────
    @FXML private TableView<Patient>           patientTable;
    @FXML private TableColumn<Patient,Integer> idCol;
    @FXML private TableColumn<Patient,String>  codeCol;
    @FXML private TableColumn<Patient,String>  nameCol;
    @FXML private TableColumn<Patient,String>  phoneCol;
    @FXML private TableColumn<Patient,String>  genderCol;
    @FXML private TableColumn<Patient,Integer> ageCol;
    @FXML private TextField searchField;

    private final PatientService service = new PatientService();
    private Patient selectedPatient = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate dropdowns
        genderCombo.setItems(FXCollections.observableArrayList(
                "Male", "Female", "Other"));
        bloodGroupCombo.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-", "Unknown"));

        // Setup table columns
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("patientId"));
        codeCol.setCellValueFactory(
                new PropertyValueFactory<>("patientCode"));
        nameCol.setCellValueFactory(
                new PropertyValueFactory<>("fullName"));
        phoneCol.setCellValueFactory(
                new PropertyValueFactory<>("phone"));
        genderCol.setCellValueFactory(
                new PropertyValueFactory<>("gender"));
        ageCol.setCellValueFactory(
                new PropertyValueFactory<>("age"));

        // When a row is clicked load it into the form for editing
        patientTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) populateFormForEdit(selected);
                });

        // Live search as user types
        searchField.textProperty().addListener((obs, old, kw) -> {
            if (kw == null || kw.isBlank())
                loadTable(service.getAllPatients());
            else
                loadTable(service.searchPatients(kw));
        });

        // Start in create mode
        updateBtn.setVisible(false);
        loadTable(service.getAllPatients());
    }

    // ════════════════════════════════
    // CREATE
    // ════════════════════════════════
    @FXML
    public void handleSave() {
        Patient p = collectFormData();
        try {
            int id = service.registerPatient(p);
            if (id > 0) {
                showStatus("Patient registered successfully! Code: "
                        + p.getPatientCode(), true);
                handleClear();
                loadTable(service.getAllPatients());
            } else {
                showStatus("Failed to save patient. Try again.", false);
            }
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), false);
        }
    }

    // ════════════════════════════════
    // UPDATE
    // ════════════════════════════════
    @FXML
    public void handleUpdate() {
        if (selectedPatient == null) {
            showStatus("Select a patient from the table to update.",
                    false);
            return;
        }
        Patient p = collectFormData();
        p.setPatientId(selectedPatient.getPatientId());
        p.setPatientCode(selectedPatient.getPatientCode());
        try {
            if (service.updatePatient(p)) {
                showStatus("Patient updated successfully!", true);
                handleClear();
                loadTable(service.getAllPatients());
            } else {
                showStatus("Update failed. Try again.", false);
            }
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), false);
        }
    }

    // ════════════════════════════════
    // DELETE
    // ════════════════════════════════
    @FXML
    public void handleDelete() {
        Patient p = patientTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            showStatus("Select a patient from the table to delete.",
                    false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete patient: " + p.getFullName());
        confirm.setContentText(
                "This will deactivate the patient record.\n" +
                        "Medical history is preserved. This cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (service.deletePatient(p.getPatientId())) {
                showStatus("Patient deactivated successfully.", true);
                handleClear();
                loadTable(service.getAllPatients());
            } else {
                showStatus("Delete failed. Try again.", false);
            }
        }
    }

    // ════════════════════════════════
    // CLEAR
    // ════════════════════════════════
    @FXML
    public void handleClear() {
        firstNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        genderCombo.getSelectionModel().clearSelection();
        bloodGroupCombo.getSelectionModel().clearSelection();
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
        medicalHistoryArea.clear();
        emergencyContactField.clear();
        emergencyPhoneField.clear();
        patientCodeLabel.setText("Auto-generated on save");
        statusLabel.setText("");
        selectedPatient = null;
        saveBtn.setVisible(true);
        updateBtn.setVisible(false);
        patientTable.getSelectionModel().clearSelection();
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
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Back error: " + e.getMessage());
        }
    }

    // ════════════════════════════════
    // HELPERS
    // ════════════════════════════════
    private void populateFormForEdit(Patient p) {
        selectedPatient = p;
        firstNameField.setText(p.getFirstName());
        lastNameField.setText(p.getLastName());
        dobPicker.setValue(p.getDateOfBirth());
        genderCombo.setValue(p.getGender());
        bloodGroupCombo.setValue(p.getBloodGroup());
        phoneField.setText(p.getPhone());
        emailField.setText(p.getEmail());
        addressArea.setText(p.getAddress());
        medicalHistoryArea.setText(p.getMedicalHistory());
        emergencyContactField.setText(p.getEmergencyContact());
        emergencyPhoneField.setText(p.getEmergencyPhone());
        patientCodeLabel.setText(p.getPatientCode());
        saveBtn.setVisible(false);
        updateBtn.setVisible(true);
    }

    private Patient collectFormData() {
        Patient p = new Patient();
        p.setFirstName(firstNameField.getText().trim());
        p.setLastName(lastNameField.getText().trim());
        p.setDateOfBirth(dobPicker.getValue());
        p.setGender(genderCombo.getValue());
        p.setBloodGroup(bloodGroupCombo.getValue());
        p.setPhone(phoneField.getText().trim());
        p.setEmail(emailField.getText().trim());
        p.setAddress(addressArea.getText().trim());
        p.setMedicalHistory(medicalHistoryArea.getText().trim());
        p.setEmergencyContact(emergencyContactField.getText().trim());
        p.setEmergencyPhone(emergencyPhoneField.getText().trim());
        return p;
    }

    private void loadTable(List<Patient> patients) {
        patientTable.setItems(
                FXCollections.observableArrayList(patients));
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
                ? "-fx-text-fill:#2E7D32; -fx-font-weight:bold;"
                : "-fx-text-fill:#C62828; -fx-font-weight:bold;");
    }
}
