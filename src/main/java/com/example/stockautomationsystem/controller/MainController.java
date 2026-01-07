package com.example.stockautomationsystem.controller;

import com.example.stockautomationsystem.exception.*;
import com.example.stockautomationsystem.MainApp;
import com.example.stockautomationsystem.model.Product;
import com.example.stockautomationsystem.model.LogEntry;
import com.example.stockautomationsystem.service.JsonService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName, colCategory;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Double> colPrice;

    @FXML private TextField nameField, priceField, stockField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private VBox sidebar;
    @FXML private Button editButton, deleteButton, logButton;
    @FXML private MenuButton userMenu;

    private final JsonService jsonService = new JsonService();
    private ObservableList<Product> productList;
    private FilteredList<Product> filteredData;
    private ObservableList<LogEntry> logList;
    private String currentUsername;

    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        productTable.getColumns().forEach(col -> col.setSortable(false));
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        productList = FXCollections.observableArrayList(jsonService.loadData());
        productTable.setItems(productList);

        logList = FXCollections.observableArrayList(jsonService.loadLogs());

        refreshCategories();
        productTable.setPlaceholder(new Label("No products available."));
    }

    private void refreshCategories() {
        Set<String> categories = new TreeSet<>();
        categories.addAll(Arrays.asList("Electronics", "Grocery", "Home", "Automotive"));
        for (Product p : productList) {
            if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                categories.add(p.getCategory());
            }
        }
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
    }

    public void setUserInfo(String username, boolean isAdmin) {
        this.currentUsername = username;
        userMenu.setText("👤 " + username);

        if (!isAdmin) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
            editButton.setVisible(false);
            editButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
            if(logButton != null) { logButton.setVisible(false); logButton.setManaged(false); }

            filteredData = new FilteredList<>(productList, p -> p.getStockQuantity() > 0);
            productTable.setItems(filteredData);
        } else {
            productTable.setItems(productList);
        }
    }

    private void addLog(String action, String details) {
        LogEntry newEntry = new LogEntry(currentUsername, action, details);
        logList.add(newEntry);
        jsonService.saveLogs(new ArrayList<>(logList));
    }

    @FXML
    private void handleUpdate() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Warning", "Please select a product from the table first!");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.getDialogPane().getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        TextField nameIn = new TextField(selected.getName());
        ComboBox<String> categoryIn = new ComboBox<>(categoryCombo.getItems());
        categoryIn.setEditable(true);
        categoryIn.setValue(selected.getCategory());
        TextField priceIn = new TextField(String.valueOf(selected.getPrice()));
        TextField stockIn = new TextField(String.valueOf(selected.getStockQuantity()));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("Name:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("Category:"), 0, 1); grid.add(categoryIn, 1, 1);
        grid.add(new Label("Price:"), 0, 2); grid.add(priceIn, 1, 2);
        grid.add(new Label("Stock:"), 0, 3); grid.add(stockIn, 1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveBtn) {
            try {
                String newName = nameIn.getText();
                String priceStr = priceIn.getText();
                String stockStr = stockIn.getText();

                // --- CUSTOM VALIDATION WITH OUR EXCEPTION ---
                if (newName == null || newName.trim().isEmpty()) {
                    throw new InvalidProductException("Product name cannot be empty!");
                }

                double newPrice = Double.parseDouble(priceStr);
                if (newPrice < 0) {
                    throw new InvalidProductException("Price cannot be negative!");
                }

                int newStock = Integer.parseInt(stockStr);
                if (newStock < 0) {
                    throw new InvalidProductException("Stock quantity cannot be negative!");
                }
                // --------------------------------------------

                String oldName = selected.getName();
                selected.setName(newName);
                selected.setCategory(categoryIn.getValue());
                selected.setPrice(newPrice);
                selected.setStockQuantity(newStock);

                save();
                addLog("UPDATE", "Updated: " + oldName + " to " + selected.getName());
                refreshCategories();
                productTable.refresh();

            } catch (InvalidProductException e) {
                // Bizim özel hata mesajımızı gösterir yanlış ürün bilgilerini yakalar
                showError("Invalid Data", e.getMessage());
            } catch (NumberFormatException e) {
                // Sayı formatı hatalarını yakalar
                showError("Format Error", "Please enter valid numeric values for Price and Stock.");
            } catch (Exception e) {
                // Diğer tüm beklenmedik hatalar
                showError("Update Error", "An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAdd() {
        try {
            String name = nameField.getText();
            String priceStr = priceField.getText();
            String stockStr = stockField.getText();

            if (name == null || name.trim().isEmpty()) {
                throw new InvalidProductException("Product name cannot be empty!");
            }

            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new InvalidProductException("Price cannot be negative!");
            }

            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                throw new InvalidProductException("Stock quantity cannot be negative!");
            }

            Product p = new Product(name, categoryCombo.getValue(), price, stock);
            productList.add(p);
            save();
            addLog("ADD", "Added: " + p.getName());
            refreshCategories();
            clearFields();

        } catch (InvalidProductException e) {
            showError("Invalid Data", e.getMessage());
        } catch (NumberFormatException e) {
            showError("Format Error", "Please enter valid numeric values for Price and Stock.");
        } catch (Exception e) {
            showError("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML private void handleDelete() {
        Product s = productTable.getSelectionModel().getSelectedItem();
        if (s != null) {
            addLog("DELETE", "Deleted: " + s.getName());
            productList.remove(s);
            save();
            refreshCategories();
        }
    }

    @FXML
    private void handleBuy() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Satın alma miktarını soran pencere
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Buy Product");
        dialog.setHeaderText("Enter the quantity you want to purchase:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);

                // Burası önemli: reduceStock metodu hata fırlatabilir!
                selected.reduceStock(qty);

                save(); // Veriyi kaydet
                addLog("PURCHASE", "Bought " + qty + " units of " + selected.getName());
                productTable.refresh();

            } catch (InsufficientStockException e) {
                // Yazdığın özel hata burada yakalanıyor
                showError("Stock Error", e.getMessage());
            } catch (NumberFormatException e) {
                // Eğer kullanıcı sayı yerine harf girerse
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

        TableColumn<LogEntry, String> t1 = new TableColumn<>("Time");
        t1.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        TableColumn<LogEntry, String> t2 = new TableColumn<>("User");
        t2.setCellValueFactory(new PropertyValueFactory<>("user"));
        TableColumn<LogEntry, String> t3 = new TableColumn<>("Action");
        t3.setCellValueFactory(new PropertyValueFactory<>("action"));
        TableColumn<LogEntry, String> t4 = new TableColumn<>("Details");
        t4.setCellValueFactory(new PropertyValueFactory<>("details"));

        table.getColumns().addAll(t1, t2, t3, t4);

        table.getColumns().forEach(col -> col.setSortable(false));

        List<LogEntry> sortedList = new ArrayList<>(logList);
        sortedList.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        FilteredList<LogEntry> fLogs = new FilteredList<>(FXCollections.observableArrayList(sortedList), p -> true);
        table.setItems(fLogs);

        ComboBox<String> filter = new ComboBox<>(FXCollections.observableArrayList("ALL", "PURCHASE", "ADD", "DELETE", "UPDATE"));
        filter.setValue("ALL");
        filter.setOnAction(e -> fLogs.setPredicate(l -> filter.getValue().equals("ALL") || l.getAction().equals(filter.getValue())));

        VBox root = new VBox(10, new Label("Filter by Type:"), filter, table);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #1e1e2f;");
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @FXML private void handleLogout() throws IOException {
        Stage stage = (Stage) productTable.getScene().getWindow();
        Scene scene = new Scene(new FXMLLoader(MainApp.class.getResource("login-view.fxml")).load(), 400, 450);
        stage.setScene(scene);
    }

    private void save() { try { jsonService.saveData(new ArrayList<>(productList)); } catch (Exception e) { e.printStackTrace(); } }
    private void clearFields() { nameField.clear(); priceField.clear(); stockField.clear(); categoryCombo.getSelectionModel().clearSelection(); }
    private void showError(String t, String c) { new Alert(Alert.AlertType.ERROR, c).showAndWait(); }
    private void showInfo(String t, String c) { new Alert(Alert.AlertType.INFORMATION, c).showAndWait(); }
}