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
            if (AuthenticationController.isAuthentic(User.getInstance().getUserPhoneNo(), oldPasswordField.getText())){
                if (Objects.equals(newPasswordField.getText(), confirmPasswordField.getText())){
                    boolean f =AuthenticationController.changePassword(User.getInstance().getUserPhoneNo(), newPasswordField.getText());
                    if (f){warning.setText("password upgraded");
                        oldPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    }
                    else warning.setText("something wrong");
                }else warning.setText("password confirmation failed");

            }else warning.setText("please enter your valid old password");
        }
    }

    private boolean check(){
        if (oldPasswordField.getText().isBlank()){
            warning.setText("give your old password");
            return false;
        }
        if (newPasswordField.getText().isBlank()){
            warning.setText("give your new password");
            return false;
        }
        if (confirmPasswordField.getText().isBlank()){
            warning.setText("confirm Password");
            return false;
        }
        return true;
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        warning.setText("");
    }
}
