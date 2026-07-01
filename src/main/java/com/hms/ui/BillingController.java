package com.hms.ui;

import com.hms.model.Bill;
import com.hms.model.Patient;
import com.hms.service.BillingService;
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

public class BillingController implements Initializable {

    // ── Form fields ──────────────────────────────────────
    @FXML private ComboBox<String>  patientCombo;
    @FXML private TextField         totalAmountField;
    @FXML private TextField         discountField;
    @FXML private TextField         taxField;
    @FXML private Label             finalAmountLabel;
    @FXML private ComboBox<String>  paymentModeCombo;
    @FXML private TextField         notesField;
    @FXML private Label             statusLabel;

    // ── Buttons ──────────────────────────────────────────
    @FXML private Button createBtn;
    @FXML private Button updateBtn;

    // ── Table ────────────────────────────────────────────
    @FXML private TableView<Bill>           billTable;
    @FXML private TableColumn<Bill,Integer> idCol;
    @FXML private TableColumn<Bill,String>  patientCol;
    @FXML private TableColumn<Bill,Double>  totalCol;
    @FXML private TableColumn<Bill,Double>  finalCol;
    @FXML private TableColumn<Bill,String>  statusCol;
    @FXML private TableColumn<Bill,String>  dateCol;

    private final BillingService billingService
            = new BillingService();
    private final PatientService patientService
            = new PatientService();

    private List<Patient> patients;
    private Bill          selectedBill = null;
    private boolean       isEditing    = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadPatientCombo();
        loadPaymentModes();
        loadTable();
        updateBtn.setVisible(false);

        // Auto-calculate final amount when totals change
        totalAmountField.textProperty().addListener(
                (o, n, v) -> recalculate());
        discountField.textProperty().addListener(
                (o, n, v) -> recalculate());
        taxField.textProperty().addListener(
                (o, n, v) -> recalculate());

        // When a row is clicked load it into the form
        billTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null && !isEditing)
                        populateFormForEdit(selected);
                });
    }

    // ── Setup table columns ──────────────────────────────
    private void setupTable() {
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("billId"));
        patientCol.setCellValueFactory(
                new PropertyValueFactory<>("patientName"));
        totalCol.setCellValueFactory(
                new PropertyValueFactory<>("totalAmount"));
        finalCol.setCellValueFactory(
                new PropertyValueFactory<>("finalAmount"));
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("paymentStatus"));
        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("billDate"));
    }

    // ── Load patients into combo ─────────────────────────
    private void loadPatientCombo() {
        patients = patientService.getAllPatients();
        patientCombo.setItems(FXCollections.observableArrayList(
                patients.stream()
                        .map(p -> p.getPatientCode()
                                + " - " + p.getFullName())
                        .toList()
        ));
    }

    // ── Load payment modes ───────────────────────────────
    private void loadPaymentModes() {
        paymentModeCombo.setItems(FXCollections.observableArrayList(
                "Cash", "Card", "Mobile Money", "Insurance"));
    }

    // ── Auto-calculate final amount ──────────────────────
    private void recalculate() {
        try {
            double total    = parseDouble(totalAmountField.getText());
            double discount = parseDouble(discountField.getText());
            double tax      = parseDouble(taxField.getText());
            double finalAmt = total - discount + tax;
            finalAmountLabel.setText(
                    String.format("UGX %.0f", finalAmt));
        } catch (Exception e) {
            finalAmountLabel.setText("UGX 0");
        }
    }

    // ════════════════════════════════
    // CREATE BILL
    // ════════════════════════════════
    @FXML
    public void handleCreate() {
        try {
            Bill b = collectFormData();
            int id = billingService.createBill(b);
            if (id > 0) {
                showStatus("Bill created successfully! ID: " + id,
                        true);
                handleClear();
                loadTable();
            } else {
                showStatus("Failed to create bill.", false);
            }
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), false);
        }
    }

    // ════════════════════════════════
    // UPDATE BILL
    // ════════════════════════════════
    @FXML
    public void handleUpdate() {
        if (selectedBill == null) {
            showStatus("Select a bill to update.", false);
            return;
        }
        try {
            Bill b = collectFormData();
            b.setBillId(selectedBill.getBillId());
            if (billingService.updateBill(b)) {
                showStatus("Bill updated successfully!", true);
                handleClear();
                loadTable();
            } else {
                showStatus("Update failed. Try again.", false);
            }
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), false);
        }
    }

    // ════════════════════════════════
    // MARK AS PAID
    // ════════════════════════════════
    @FXML
    public void handleMarkPaid() {
        if (selectedBill == null) {
            showStatus("Select a bill to mark as paid.", false);
            return;
        }
        if (paymentModeCombo.getValue() == null) {
            showStatus("Please select a payment mode first.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark as Paid");
        confirm.setHeaderText("Mark bill as paid for: "
                + selectedBill.getPatientName());
        confirm.setContentText(
                "Amount: UGX " + selectedBill.getFinalAmount()
                        + "\nPayment mode: " + paymentModeCombo.getValue());
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (billingService.markAsPaid(
                    selectedBill.getBillId(),
                    paymentModeCombo.getValue())) {
                showStatus("Bill marked as paid successfully!", true);
                handleClear();
                loadTable();
            } else {
                showStatus("Failed to mark as paid.", false);
            }
        }
    }

    // ════════════════════════════════
    // DELETE BILL
    // ════════════════════════════════
    @FXML
    public void handleDelete() {
        if (selectedBill == null) {
            showStatus("Select a bill to delete.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Bill");
        confirm.setHeaderText("Delete bill for: "
                + selectedBill.getPatientName());
        confirm.setContentText(
                "This will permanently delete the bill record.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (billingService.deleteBill(selectedBill.getBillId())) {
                showStatus("Bill deleted successfully.", true);
                handleClear();
                loadTable();
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
        isEditing = false;
        patientCombo.getSelectionModel().clearSelection();
        patientCombo.setDisable(false);
        totalAmountField.clear();
        discountField.clear();
        taxField.clear();
        finalAmountLabel.setText("UGX 0");
        paymentModeCombo.getSelectionModel().clearSelection();
        notesField.clear();
        statusLabel.setText("");
        selectedBill = null;
        createBtn.setVisible(true);
        updateBtn.setVisible(false);
        billTable.getSelectionModel().clearSelection();
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
            Stage stage = (Stage) notesField.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Back error: " + e.getMessage());
        }
    }

    // ════════════════════════════════
    // HELPERS
    // ════════════════════════════════
    private void populateFormForEdit(Bill b) {
        isEditing = true;
        selectedBill = b;

        // Match patient in combo
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId() == b.getPatientId()) {
                patientCombo.getSelectionModel().select(i);
                break;
            }
        }

        totalAmountField.setText(
                String.valueOf(b.getTotalAmount()));
        discountField.setText(
                String.valueOf(b.getDiscount()));
        taxField.setText(
                String.valueOf(b.getTax()));
        finalAmountLabel.setText(
                String.format("UGX %.0f", b.getFinalAmount()));
        if (b.getPaymentMode() != null)
            paymentModeCombo.setValue(b.getPaymentMode());
        notesField.setText(
                b.getNotes() != null ? b.getNotes() : "");

        // Lock patient when editing
        patientCombo.setDisable(true);
        createBtn.setVisible(false);
        updateBtn.setVisible(true);
        isEditing = false;
    }

    private Bill collectFormData() {
        Bill b = new Bill();
        int patientIndex = patientCombo.getSelectionModel()
                .getSelectedIndex();
        if (patientIndex >= 0)
            b.setPatientId(
                    patients.get(patientIndex).getPatientId());
        b.setTotalAmount(parseDouble(totalAmountField.getText()));
        b.setDiscount(parseDouble(discountField.getText()));
        b.setTax(parseDouble(taxField.getText()));
        b.recalculate();
        if (paymentModeCombo.getValue() != null)
            b.setPaymentMode(paymentModeCombo.getValue());
        b.setNotes(notesField.getText().trim());
        return b;
    }

    private double parseDouble(String text) {
        try {
            return text == null || text.isBlank()
                    ? 0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void loadTable() {
        billTable.setItems(FXCollections.observableArrayList(
                billingService.getAllBills()));
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
                ? "-fx-text-fill:#2E7D32; -fx-font-weight:bold;"
                : "-fx-text-fill:#C62828; -fx-font-weight:bold;");
    }
}