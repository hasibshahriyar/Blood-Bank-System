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
import javafx.stage.Modality;
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
//        changeStage(event);
        if (!inputFieldValidate()){
            return;
        }
        String emailOrPhone = emailOrPhoneInputField.getText();
        String password = passwordInputField.getText();
        boolean isAuthenticate;
        if (emailOrPhone.contains(".com")){
            try {
                isAuthenticate= AuthenticationController.authenticateWithEmail(emailOrPhone,password);
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                isAuthenticate=AuthenticationController.authenticateWithPhoneNo(emailOrPhone,password);
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }

        }
        if (!isAuthenticate){
            System.out.println("not");

            showWarning.setText("invalid Email or password");
        }else {
            System.out.println("yes");

            changeStage(event);
        }
    }

    @FXML
    protected void onRegisterButtonClick(ActionEvent event){
        try{
            AnchorPane anchorPane = FxmlLoader.getAnchorPane("register-menu.fxml");

            borderPane.setCenter(anchorPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeStage(ActionEvent event){
        try{
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
            loginStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            loginStage.setTitle("Blood Bank");
            loginStage.setScene(new Scene(root));
            loginStage.getIcons().add(new Image("E:\\IdeaProjects\\blood-donation-system\\src\\main\\resources\\com\\mahbubalam\\blooddonationsystem\\img\\Rokto2.png"));
            loginStage.setResizable(false);
            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean inputFieldValidate(){
        if (emailOrPhoneInputField.getText().isBlank()){
            showWarning.setText("email or phone is required");
            return false;
        }
        if (passwordInputField.getText().isBlank()){
            showWarning.setText("password is required");
            return false;
        }
        return true;
    }

    public void onClickForgatePassword(ActionEvent event) throws IOException {
        AnchorPane anchorPane = FxmlLoader.getAnchorPane("forget-password-view.fxml");

        borderPane.setCenter(anchorPane);
    }
}