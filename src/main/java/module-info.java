module com.example.stockautomationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    opens com.example.stockautomationsystem.model to javafx.base, com.fasterxml.jackson.databind;
    opens com.example.stockautomationsystem.controller to javafx.fxml;
    exports com.example.stockautomationsystem;
}