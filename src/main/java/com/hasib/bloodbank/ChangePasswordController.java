package com.hasib.bloodbank;

import com.jfoenix.controls.JFXButton;
import com.hasib.bloodbank.server.controller.AuthenticationController;
import com.hasib.bloodbank.singleton.User;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.Objects;

public class ChangePasswordController {
    public PasswordField oldPasswordField;
    public PasswordField confirmPasswordField;
    public PasswordField newPasswordField;
    public Text warning;
    public JFXButton saveButton;

    public void onClickChangePassword(ActionEvent event) throws SQLException, ClassNotFoundException {
        if (check()){
            if (AuthenticationController.authenticateWithPhoneNo(User.getInstance().getUserPhoneNo(), oldPasswordField.getText())){
                if (Objects.equals(newPasswordField.getText(), confirmPasswordField.getText())){
                    boolean f = AuthenticationController.changePassword(User.getInstance().getUserPhoneNo(), newPasswordField.getText());
                    if (f){
                        warning.setText("✅ Password updated successfully!");
                        warning.setStyle("-fx-fill: #28a745;");
                        oldPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    }
                    else {
                        warning.setText("❌ Something went wrong. Please try again.");
                        warning.setStyle("-fx-fill: #dc3545;");
                    }
                }else {
                    warning.setText("⚠️ Password confirmation failed. Passwords don't match.");
                    warning.setStyle("-fx-fill: #ffc107;");
                }
            }else {
                warning.setText("❌ Please enter your valid old password");
                warning.setStyle("-fx-fill: #dc3545;");
            }
        }
    }

    private boolean check(){
        if (oldPasswordField.getText().isBlank()){
            warning.setText("⚠️ Please enter your old password");
            warning.setStyle("-fx-fill: #ffc107;");
            return false;
        }
        if (newPasswordField.getText().isBlank()){
            warning.setText("⚠️ Please enter a new password");
            warning.setStyle("-fx-fill: #ffc107;");
            return false;
        }
        if (confirmPasswordField.getText().isBlank()){
            warning.setText("⚠️ Please confirm your new password");
            warning.setStyle("-fx-fill: #ffc107;");
            return false;
        }
        if (newPasswordField.getText().length() < 6){
            warning.setText("⚠️ Password must be at least 6 characters long");
            warning.setStyle("-fx-fill: #ffc107;");
            return false;
        }
        return true;
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        warning.setText("");
    }
}
