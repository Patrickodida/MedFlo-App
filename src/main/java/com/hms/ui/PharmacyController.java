package com.hms.ui;

import com.hms.model.InventoryItem;
import com.hms.service.InventoryService;
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

public class PharmacyController implements Initializable {

    // ── Form fields ──────────────────────────────────────
    @FXML private TextField         itemNameField;
    @FXML private ComboBox<String>  categoryCombo;
    @FXML private TextField         quantityField;
    @FXML private TextField         unitField;
    @FXML private TextField         unitPriceField;
    @FXML private DatePicker        expiryPicker;
    @FXML private TextField         supplierField;
    @FXML private TextField         reorderLevelField;
    @FXML private Label             statusLabel;
    @FXML private Label             lowStockLabel;

    // ── Buttons ──────────────────────────────────────────
    @FXML private Button addBtn;
    @FXML private Button updateBtn;

    // ── Table ────────────────────────────────────────────
    @FXML private TableView<InventoryItem>           itemTable;
    @FXML private TableColumn<InventoryItem,Integer> idCol;
    @FXML private TableColumn<InventoryItem,String>  nameCol;
    @FXML private TableColumn<InventoryItem,String>  categoryCol;
    @FXML private TableColumn<InventoryItem,Integer> quantityCol;
    @FXML private TableColumn<InventoryItem,String>  unitCol;
    @FXML private TableColumn<InventoryItem,Double>  priceCol;
    @FXML private TableColumn<InventoryItem,String>  expiryCol;
    @FXML private TableColumn<InventoryItem,String>  supplierCol;

    // ── Search ───────────────────────────────────────────
    @FXML private TextField searchField;

    private final InventoryService inventoryService
            = new InventoryService();

    private InventoryItem selectedItem = null;
    private boolean       isEditing    = false;
    private List<InventoryItem> allItems;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadCategoryCombo();
        loadTable();
        updateBtn.setVisible(false);

        // When a row is clicked load it into the form
        itemTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null && !isEditing)
                        populateFormForEdit(selected);
                });

        // Live search
        searchField.textProperty().addListener((obs, old, kw) -> {
            if (kw == null || kw.isBlank()) {
                itemTable.setItems(
                        FXCollections.observableArrayList(allItems));
            } else {
                String lower = kw.toLowerCase();
                itemTable.setItems(
                        FXCollections.observableArrayList(
                                allItems.stream()
                                        .filter(i -> i.getItemName()
                                                .toLowerCase().contains(lower)
                                                || (i.getCategory() != null &&
                                                i.getCategory().toLowerCase()
                                                        .contains(lower)))
                                        .toList()
                        )
                );
            }
        });
    }

    // ── Setup table columns ──────────────────────────────
    private void setupTable() {
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("itemId"));
        nameCol.setCellValueFactory(
                new PropertyValueFactory<>("itemName"));
        categoryCol.setCellValueFactory(
                new PropertyValueFactory<>("category"));
        quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));
        unitCol.setCellValueFactory(
                new PropertyValueFactory<>("unit"));
        priceCol.setCellValueFactory(
                new PropertyValueFactory<>("unitPrice"));
        expiryCol.setCellValueFactory(
                new PropertyValueFactory<>("expiryDate"));
        supplierCol.setCellValueFactory(
                new PropertyValueFactory<>("supplierName"));

        // Colour code quantity column — red if low stock
        quantityCol.setCellFactory(col ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(Integer qty, boolean empty) {
                        super.updateItem(qty, empty);
                        if (empty || qty == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(String.valueOf(qty));
                            InventoryItem item = getTableView()
                                    .getItems().get(getIndex());
                            if (item.isLowStock()) {
                                setStyle("-fx-text-fill:#C62828;"
                                        + "-fx-font-weight:bold;");
                            } else {
                                setStyle("-fx-text-fill:#2E7D32;"
                                        + "-fx-font-weight:bold;");
                            }
                        }
                    }
                }
        );
    }

    // ── Load categories ──────────────────────────────────
    private void loadCategoryCombo() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Medicine", "Equipment", "Supply", "Other"));
    }

    // ════════════════════════════════
    // ADD ITEM
    // ════════════════════════════════
    @FXML
    public void handleAdd() {
        try {
            InventoryItem item = collectFormData();
            int id = inventoryService.addItem(item);
            if (id > 0) {
                showStatus("Item added successfully!", true);
                handleClear();
                loadTable();
            } else {
                showStatus("Failed to add item.", false);
            }
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), false);
        }
    }

    // ════════════════════════════════
    // UPDATE ITEM
    // ════════════════════════════════
    @FXML
    public void handleUpdate() {
        if (selectedItem == null) {
            showStatus("Select an item to update.", false);
            return;
        }
        try {
            InventoryItem item = collectFormData();
            item.setItemId(selectedItem.getItemId());
            if (inventoryService.updateItem(item)) {
                showStatus("Item updated successfully!", true);
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
    // DELETE ITEM
    // ════════════════════════════════
    @FXML
    public void handleDelete() {
        if (selectedItem == null) {
            showStatus("Select an item to delete.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Item");
        confirm.setHeaderText(
                "Delete: " + selectedItem.getItemName());
        confirm.setContentText(
                "This will permanently remove this item from inventory.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (inventoryService.deleteItem(
                    selectedItem.getItemId())) {
                showStatus("Item deleted successfully.", true);
                handleClear();
                loadTable();
            } else {
                showStatus("Delete failed. Try again.", false);
            }
        }
    }

    // ════════════════════════════════
    // SHOW LOW STOCK ONLY
    // ════════════════════════════════
    @FXML
    public void handleShowLowStock() {
        List<InventoryItem> lowStock =
                inventoryService.getLowStockItems();
        itemTable.setItems(
                FXCollections.observableArrayList(lowStock));
        showStatus("Showing " + lowStock.size()
                + " low stock items.", true);
    }

    // ════════════════════════════════
    // SHOW ALL ITEMS
    // ════════════════════════════════
    @FXML
    public void handleShowAll() {
        loadTable();
        statusLabel.setText("");
    }

    // ════════════════════════════════
    // CLEAR
    // ════════════════════════════════
    @FXML
    public void handleClear() {
        isEditing = false;
        itemNameField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        quantityField.clear();
        unitField.clear();
        unitPriceField.clear();
        expiryPicker.setValue(null);
        supplierField.clear();
        reorderLevelField.clear();
        statusLabel.setText("");
        selectedItem = null;
        addBtn.setVisible(true);
        updateBtn.setVisible(false);
        itemTable.getSelectionModel().clearSelection();
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
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Back error: " + e.getMessage());
        }
    }

    // ════════════════════════════════
    // HELPERS
    // ════════════════════════════════
    private void populateFormForEdit(InventoryItem item) {
        isEditing = true;
        selectedItem = item;
        itemNameField.setText(item.getItemName());
        categoryCombo.setValue(item.getCategory());
        quantityField.setText(String.valueOf(item.getQuantity()));
        unitField.setText(item.getUnit());
        unitPriceField.setText(String.valueOf(item.getUnitPrice()));
        expiryPicker.setValue(item.getExpiryDate());
        supplierField.setText(item.getSupplierName());
        reorderLevelField.setText(
                String.valueOf(item.getReorderLevel()));
        addBtn.setVisible(false);
        updateBtn.setVisible(true);
        isEditing = false;
    }

    private InventoryItem collectFormData() {
        InventoryItem item = new InventoryItem();
        item.setItemName(itemNameField.getText().trim());
        item.setCategory(categoryCombo.getValue());
        item.setQuantity(parseInt(quantityField.getText()));
        item.setUnit(unitField.getText().trim());
        item.setUnitPrice(parseDouble(unitPriceField.getText()));
        item.setExpiryDate(expiryPicker.getValue());
        item.setSupplierName(supplierField.getText().trim());
        item.setReorderLevel(parseInt(reorderLevelField.getText()));
        return item;
    }

    private void loadTable() {
        allItems = inventoryService.getAllItems();
        itemTable.setItems(
                FXCollections.observableArrayList(allItems));
        // Show low stock alert
        long lowCount = allItems.stream()
                .filter(InventoryItem::isLowStock).count();
        if (lowCount > 0) {
            lowStockLabel.setText(
                    "⚠ " + lowCount + " item(s) are low on stock!");
            lowStockLabel.setStyle(
                    "-fx-text-fill:#C62828; -fx-font-weight:bold;");
        } else {
            lowStockLabel.setText("All stock levels are healthy.");
            lowStockLabel.setStyle("-fx-text-fill:#2E7D32;");
        }
    }

    private int parseInt(String text) {
        try {
            return text == null || text.isBlank()
                    ? 0 : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) { return 0; }
    }

    private double parseDouble(String text) {
        try {
            return text == null || text.isBlank()
                    ? 0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) { return 0; }
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
                ? "-fx-text-fill:#2E7D32; -fx-font-weight:bold;"
                : "-fx-text-fill:#C62828; -fx-font-weight:bold;");
    }
}