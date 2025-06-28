package com.hasib.bloodbank;

import com.hasib.bloodbank.server.controller.AuthenticationController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {

    public TextField emailOrPhoneInputField;
    public PasswordField passwordInputField;
    public Text showWarning;
    @FXML
    private Stage loginStage;

    @FXML
    private Parent root;

    @FXML
    BorderPane borderPane;

    @FXML
    protected void onLoginButtonClick(ActionEvent event){
        if (!inputFieldValidate()){
            return;
        }

        String emailOrPhone = emailOrPhoneInputField.getText().trim();
        String password = passwordInputField.getText();
        boolean isAuthenticate = false;

        try {
            if (emailOrPhone.contains("@") && emailOrPhone.contains(".")){
                isAuthenticate = AuthenticationController.authenticateWithEmail(emailOrPhone, password);
            } else {
                isAuthenticate = AuthenticationController.authenticateWithPhoneNo(emailOrPhone, password);
            }

            if (!isAuthenticate){
                showWarning.setText("Invalid email/phone or password");
                showWarning.setVisible(true);
            } else {
                showWarning.setVisible(false);
                changeStage(event);
            }
        } catch (ClassNotFoundException | SQLException e) {
            showWarning.setText("Database connection error. Please try again.");
            showWarning.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRegisterButtonClick(ActionEvent event){
        try{
            AnchorPane anchorPane = FxmlLoader.getAnchorPane("register-menu.fxml");
            borderPane.setCenter(anchorPane);
        } catch (Exception e) {
            showWarning.setText("Error loading registration form");
            showWarning.setVisible(true);
            e.printStackTrace();
        }
    }

    private void changeStage(ActionEvent event){
        try{
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
            loginStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            loginStage.setTitle("Blood Bank Management System");
            loginStage.setScene(new Scene(root));

            // Try to load icon from resources, fall back gracefully if not found
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/bloodtype_FILL0_wght400_GRAD0_opsz48.png")));
                loginStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Could not load application icon");
            }

            loginStage.setResizable(false);
            loginStage.show();
        } catch (Exception e) {
            showWarning.setText("Error opening main application");
            showWarning.setVisible(true);
            e.printStackTrace();
        }
    }

    private boolean inputFieldValidate(){
        if (emailOrPhoneInputField.getText().isBlank()){
            showWarning.setText("Email or phone number is required");
            showWarning.setVisible(true);
            return false;
        }
        if (passwordInputField.getText().isBlank()){
            showWarning.setText("Password is required");
            showWarning.setVisible(true);
            return false;
        }

        String emailOrPhone = emailOrPhoneInputField.getText().trim();
        // Basic email validation
        if (emailOrPhone.contains("@")) {
            if (!emailOrPhone.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showWarning.setText("Please enter a valid email address");
                showWarning.setVisible(true);
                return false;
            }
        } else {
            // Basic phone validation for Bangladesh
            if (!emailOrPhone.matches("^01[3-9]\\d{8}$")) {
                showWarning.setText("Please enter a valid phone number (e.g., 01712345678)");
                showWarning.setVisible(true);
                return false;
            }
        }

        return true;
    }

    public void onClickForgatePassword(ActionEvent event) throws IOException {
        try {
            AnchorPane anchorPane = FxmlLoader.getAnchorPane("forget-password-view.fxml");
            borderPane.setCenter(anchorPane);
        } catch (Exception e) {
            showWarning.setText("Error loading password reset form");
            showWarning.setVisible(true);
            e.printStackTrace();
        }
    }
}