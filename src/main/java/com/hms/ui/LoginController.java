package com.hms.ui;

import com.hms.model.User;
import com.hms.service.AuthService;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField         usernameField;
    @FXML private PasswordField     passwordField;
    @FXML private Label             errorLabel;
    @FXML private Button            loginButton;
    @FXML private VBox              loginCard;
    @FXML private ProgressIndicator loadingSpinner;

    private final AuthService auth = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Fade in the card when screen loads
        loginCard.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), loginCard);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        // Allow Enter key in password field to trigger login
        passwordField.setOnAction(e -> handleLogin());

        // Hide spinner by default
        loadingSpinner.setVisible(false);
        errorLabel.setVisible(false);

        // Clear error when user starts typing
        usernameField.textProperty().addListener((o, n, v) -> clearError());
        passwordField.textProperty().addListener((o, n, v) -> clearError());
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs before touching the database
        String validationError = auth.validateInputs(username, password);
        if (validationError != null) {
            showError(validationError);
            shakeCard();
            return;
        }

        setLoading(true);

        // Run DB call on a background thread so the UI does not freeze
        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                return auth.login(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            User user = task.getValue();
            if (user != null) {
                navigateToDashboard(user);
            } else {
                showError("Invalid username or password.");
                shakeCard();
                passwordField.clear();
            }
        });

        task.setOnFailed(e -> {
            setLoading(false);
            showError("Connection error. Check your database.");
        });

        new Thread(task).start();
    }

    @FXML
    public void handleClear() {
        usernameField.clear();
        passwordField.clear();
        clearError();
        usernameField.requestFocus();
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }

    @FXML
    public void handleForgotPassword() {
        showError("Contact the System Administrator to reset your password.");
    }

    private void navigateToDashboard(User user) {
        String fxmlPath = auth.getDashboardPath(user.getRole());
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pass the logged-in user to the dashboard controller
            Object controller = loader.getController();
            if (controller instanceof DashboardController dc) {
                dc.setUser(user);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 780);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("Medicare Hospital — " + user.getRole() + " Dashboard");

        } catch (IOException e) {
            showError("Dashboard screen not built yet for role: " + user.getRole());
            System.err.println("Could not load: " + fxmlPath + " — " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private void setLoading(boolean on) {
        loadingSpinner.setVisible(on);
        loginButton.setDisable(on);
        loginButton.setText(on ? "Signing in..." : "Sign In");
    }

    private void shakeCard() {
        TranslateTransition t = new TranslateTransition(
                Duration.millis(60), loginCard);
        t.setFromX(0);
        t.setByX(10);
        t.setCycleCount(6);
        t.setAutoReverse(true);
        t.play();
    }
}
