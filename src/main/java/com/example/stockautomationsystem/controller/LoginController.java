package com.example.stockautomationsystem.controller;

import com.example.stockautomationsystem.exception.*;
import com.example.stockautomationsystem.MainApp;
import com.example.stockautomationsystem.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private static final List<User> registeredUsers = new ArrayList<>();

    static {
        registeredUsers.add(new User("admin", "admin", true));
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        try {
            if (user.isEmpty() || pass.isEmpty()) {
                throw new InvalidProductException("Username or Password cannot be empty!");
                // Not: InvalidProduct yerine daha genel bir InvalidInputException da yazılabilir
            }

            for (User u : registeredUsers) {
                if (u.getUsername().equalsIgnoreCase(user)) {
                    // Eğer kullanıcı varsa hatayı fırlatıyoruz
                    throw new UserAlreadyExistsException(user);
                }
            }

            registeredUsers.add(new User(user, pass, false));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful!");
            clearFields();

        } catch (UserAlreadyExistsException | InvalidProductException e) {
            // Birden fazla hatayı aynı blokta yakalama (Multi-catch)
            showAlert(Alert.AlertType.ERROR, "Registration Error", e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        try {
            if (user.isEmpty() || pass.isEmpty()) {
                throw new AuthenticationException("Username or Password cannot be empty!");
            }

            boolean authenticated = false;
            for (User u : registeredUsers) {
                if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                    authenticated = true;
                    switchToMainApp(u);
                    break;
                }
            }

            if (!authenticated) {
                // Kimlik bilgileri uyuşmazsa hatayı fırlatıyoruz
                throw new AuthenticationException("Invalid username or password!");
            }

        } catch (AuthenticationException e) {
            // Kendi hata sınıfımızdan gelen mesajı gösteriyoruz
            showAlert(Alert.AlertType.ERROR, "Authentication Failed", e.getMessage());
        } catch (IOException e) {
            // Dosya yükleme hatası (Checked Exception)
            showAlert(Alert.AlertType.ERROR, "System Error", "Main screen could not be loaded!");
        }
    }

    private void switchToMainApp(User user) throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);

        MainController mainController = fxmlLoader.getController();

        mainController.setUserInfo(user.getUsername(), user.isAdmin());

        stage.setScene(scene);
        stage.setTitle("Stock Automation System - Logged in as: " + user.getUsername());
        stage.centerOnScreen();
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}