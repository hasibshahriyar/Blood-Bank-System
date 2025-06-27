package com.hasib.bloodbank;

import com.jfoenix.controls.JFXButton;
import com.hasib.bloodbank.singleton.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

public class MainMenuController implements Initializable {
    public JFXButton userProfileButton;
    public JFXButton logOutButton;
    public JFXButton requestBloodButton;
    public JFXButton donateBloodButton;
    public JFXButton changePasswordButton;
    public JFXButton notificationsButton;
    public BorderPane borderPane;
    public Text txt;
    User user = User.getInstance();
    @FXML
    private Label bloodGroup;
    @FXML
    private Label gender;
    @FXML
    private Label editProfileButton;
    @FXML
    private Label name;
    @FXML
    private Label nidNum;
    @FXML
    private Label mobileNum;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private Parent root;
    @FXML
    private Stage mainMenuStage;
    @FXML
    private Circle circle;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        Image img = new Image("bal.png");
//        circle.setFill(new ImagePattern(img));
    }


    @FXML
    public void onClickUserProfileButton(ActionEvent event) {

        try {
            AnchorPane anchorPane = FxmlLoader.getAnchorPane("profile-view.fxml");
            borderPane.setCenter(anchorPane);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickRequestBloodButton(ActionEvent event) {
        try {
            AnchorPane anchorPane = FxmlLoader.getAnchorPane("request-blood-view.fxml");
            borderPane.setCenter(anchorPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickDonateBloodButton(ActionEvent event) {
        try {
            AnchorPane anchorPane = FxmlLoader.getAnchorPane("donate-blood-view.fxml");
            borderPane.setCenter(anchorPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickNotificationsButton(ActionEvent event) {
        try {
            AnchorPane anchorPane = FxmlLoader.getAnchorPane("notification-view.fxml");
            borderPane.setCenter(anchorPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickLogOutButton(ActionEvent event) {
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login-view.fxml")));
            mainMenuStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            mainMenuStage.setTitle("BloodBank");
            mainMenuStage.setScene(new Scene(root));
            mainMenuStage.getIcons().add(new Image("E:\\IdeaProjects\\blood-donation-system\\src\\main\\resources\\com\\mahbubalam\\blooddonationsystem\\Rokto2.png"));
            mainMenuStage.setResizable(false);
            mainMenuStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onClickUserChangePasswordButton(ActionEvent event) throws IOException {
        AnchorPane anchorPane = FxmlLoader.getAnchorPane("change-password.fxml");
        borderPane.setCenter(anchorPane);
    }

    public void onClickEditProfile(ActionEvent event) throws IOException {
        AnchorPane anchorPane = FxmlLoader.getAnchorPane("edit-profile-view.fxml");
        borderPane.setCenter(anchorPane);
    }

}
