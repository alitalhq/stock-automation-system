package com.example.stockautomationsystem.controller;

import com.example.stockautomationsystem.exception.*;
import com.example.stockautomationsystem.MainApp;
import com.example.stockautomationsystem.model.Product;
import com.example.stockautomationsystem.model.LogEntry;
import com.example.stockautomationsystem.service.JsonService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.util.*;

public class MainController {
    // UI Bileşenleri
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName, colCategory;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TextField nameField, priceField, stockField, searchField; // searchField eklendi
    @FXML private ComboBox<String> categoryCombo;
    @FXML private VBox sidebar;
    @FXML private Button editButton, deleteButton, logButton;
    @FXML private MenuButton userMenu;

    // Veri Yönetimi
    private final JsonService jsonService = new JsonService();
    private ObservableList<Product> productList;
    private FilteredList<Product> filteredData;
    private ObservableList<LogEntry> logList;
    private String currentUsername;

    @FXML
    public void initialize() {
        // Tablo Sütun Eşleştirmeleri
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Verileri Yükle
        productList = FXCollections.observableArrayList(jsonService.loadData());
        logList = FXCollections.observableArrayList(jsonService.loadLogs());

        // --- DINAMIK ARAMA (SEARCH) MANTIĞI ---
        filteredData = new FilteredList<>(productList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();

                if (product.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (product.getCategory().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);
        // --------------------------------------

        refreshCategories();
        productTable.setPlaceholder(new Label("No products matching your search."));
    }

    public void setUserInfo(String username, boolean isAdmin) {
        this.currentUsername = username;
        userMenu.setText("👤 " + username);

        if (!isAdmin) {
            // Admin olmayan kullanıcılar için kısıtlamalar
            sidebar.setVisible(false); sidebar.setManaged(false);
            editButton.setVisible(false); editButton.setManaged(false);
            deleteButton.setVisible(false); deleteButton.setManaged(false);
            if(logButton != null) { logButton.setVisible(false); logButton.setManaged(false); }

            // Kullanıcı sadece stokta olanları görsün (Search ile birleşik çalışır)
            filteredData.setPredicate(p -> p.getStockQuantity() > 0);
        }
    }

    @FXML
    private void handleAdd() {
        try {
            validateInputs(nameField.getText(), priceField.getText(), stockField.getText());

            Product p = new Product(
                    nameField.getText(),
                    categoryCombo.getValue(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(stockField.getText())
            );

            productList.add(p);
            save();
            addLog("ADD", "Added new product: " + p.getName());
            refreshCategories();
            clearFields();
            showInfo("Success", "Product added successfully.");

        } catch (InvalidProductException e) {
            showError("Validation Error", e.getMessage());
        } catch (NumberFormatException e) {
            showError("Format Error", "Please enter valid numeric values for Price and Stock.");
        }
    }

    @FXML
    private void handleUpdate() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Warning", "Please select a product to edit!");
            return;
        }

        // Profesyonel Dialog Tasarımı
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Product: " + selected.getName());
        dialog.getDialogPane().getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());

        TextField nameIn = new TextField(selected.getName());
        ComboBox<String> catIn = new ComboBox<>(categoryCombo.getItems());
        catIn.setEditable(true); catIn.setValue(selected.getCategory());
        TextField priceIn = new TextField(String.valueOf(selected.getPrice()));
        TextField stockIn = new TextField(String.valueOf(selected.getStockQuantity()));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("Name:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("Category:"), 0, 1); grid.add(catIn, 1, 1);
        grid.add(new Label("Price:"), 0, 2); grid.add(priceIn, 1, 2);
        grid.add(new Label("Stock:"), 0, 3); grid.add(stockIn, 1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveBtn = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveBtn) {
            try {
                validateInputs(nameIn.getText(), priceIn.getText(), stockIn.getText());

                String oldName = selected.getName();
                selected.setName(nameIn.getText());
                selected.setCategory(catIn.getValue());
                selected.setPrice(Double.parseDouble(priceIn.getText()));
                selected.setStockQuantity(Integer.parseInt(stockIn.getText()));

                save();
                addLog("UPDATE", "Updated " + oldName + " info.");
                productTable.refresh();
                refreshCategories();

            } catch (Exception e) {
                showError("Update Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // PDF raporundaki onay kutusu mantığı [cite: 309, 310]
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Confirmation");
            alert.setHeaderText("Deleting: " + selected.getName());
            alert.setContentText("Are you sure you want to delete this product? This action cannot be undone.");

            // Kullanıcı 'YES' derse silme işlemini yap
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                addLog("DELETE", "Removed product: " + selected.getName());
                productList.remove(selected);
                save();
                refreshCategories();
            }
        }
    }
    @FXML
    private void handleBuy() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Purchase Product");
        dialog.setHeaderText("Buying: " + selected.getName());
        dialog.setContentText("Please enter the quantity:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);

                // NEGATİF / 0 KONTROLÜ
                if (qty <= 0) {
                    showError("Input Error", "Quantity must be greater than 0.");
                    return;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Purchase");
                confirm.setContentText("Are you sure you want to buy " + qty + " units?");

                if (confirm.showAndWait().get() == ButtonType.OK) {
                    selected.reduceStock(qty);
                    save();
                    addLog("PURCHASE", "Bought " + qty + " units of " + selected.getName());
                    productTable.refresh();
                }
            } catch (InsufficientStockException e) {
                showError("Stock Error", e.getMessage());
            } catch (NumberFormatException e) {
                showError("Input Error", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void handleViewLogs() {
        Stage stage = new Stage();
        stage.setTitle("System Logs");

        TableView<LogEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LogEntry, String> t1 = new TableColumn<>("Timestamp");
        t1.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        TableColumn<LogEntry, String> t2 = new TableColumn<>("User");
        t2.setCellValueFactory(new PropertyValueFactory<>("user"));
        TableColumn<LogEntry, String> t3 = new TableColumn<>("Action");
        t3.setCellValueFactory(new PropertyValueFactory<>("action"));
        TableColumn<LogEntry, String> t4 = new TableColumn<>("Details");
        t4.setCellValueFactory(new PropertyValueFactory<>("details"));

        table.getColumns().addAll(t1, t2, t3, t4);

        // Son loglar en üstte görünsün
        ObservableList<LogEntry> sortedLogs = FXCollections.observableArrayList(logList);
        sortedLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        FilteredList<LogEntry> fLogs = new FilteredList<>(sortedLogs, p -> true);
        table.setItems(fLogs);

        ComboBox<String> filter = new ComboBox<>(FXCollections.observableArrayList("ALL", "PURCHASE", "ADD", "DELETE", "UPDATE"));
        filter.setValue("ALL");
        filter.setOnAction(e -> fLogs.setPredicate(l -> filter.getValue().equals("ALL") || l.getAction().equals(filter.getValue())));

        VBox root = new VBox(10, new Label("Filter Logs:"), filter, table);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #1e1e2f;");
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(root, 850, 500);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    // Yardımcı Metotlar
    private void validateInputs(String name, String price, String stock) throws InvalidProductException {
        if (name == null || name.trim().isEmpty()) throw new InvalidProductException("Name is required!");
        try {
            if (Double.parseDouble(price) < 0) throw new InvalidProductException("Price cannot be negative!");
            if (Integer.parseInt(stock) < 0) throw new InvalidProductException("Stock cannot be negative!");
        } catch (NumberFormatException e) {
            throw new InvalidProductException("Price and Stock must be numeric!");
        }
    }

    private void refreshCategories() {
        Set<String> categories = new TreeSet<>(Arrays.asList("Electronics", "Grocery", "Home", "Automotive"));
        productList.forEach(p -> { if (p.getCategory() != null) categories.add(p.getCategory()); });
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
    }

    private void addLog(String action, String details) {
        logList.add(new LogEntry(currentUsername, action, details));
        jsonService.saveLogs(new ArrayList<>(logList));
    }

    private void save() {
        try {
            jsonService.saveData(new ArrayList<>(productList));
        } catch (Exception e) {
            showError("Data Error", "Could not save to JSON file!");
        }
    }

    private void clearFields() {
        nameField.clear(); priceField.clear(); stockField.clear();
        categoryCombo.getSelectionModel().clearSelection();
    }

    private void showError(String t, String c) { new Alert(Alert.AlertType.ERROR, c).showAndWait(); }
    private void showInfo(String t, String c) { new Alert(Alert.AlertType.INFORMATION, c).showAndWait(); }

    @FXML private void handleLogout() throws IOException {
        Stage stage = (Stage) productTable.getScene().getWindow();
        stage.setScene(new Scene(new FXMLLoader(MainApp.class.getResource("login-view.fxml")).load(), 400, 450));
    }
}