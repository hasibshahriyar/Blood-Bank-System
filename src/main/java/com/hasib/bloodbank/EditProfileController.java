package com.hasib.bloodbank;

import com.jfoenix.controls.JFXButton;
import com.hasib.bloodbank.server.controller.AddressController;
import com.hasib.bloodbank.server.controller.AuthenticationController;
import com.hasib.bloodbank.server.controller.PersonController;
import com.hasib.bloodbank.server.entity.Address;
import com.hasib.bloodbank.singleton.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class EditProfileController extends MainMenuController implements Initializable {
    public TextField firstNameTextField;
    public PasswordField confirmPasswordField;
    public TextField thanaTextField;
    public TextField lastNameTextField;
    public JFXButton saveButton;
    public Text warning;
    @FXML
    private ComboBox<String> divisionComboBox = new ComboBox<>();
    @FXML
    private ComboBox<String> districtComboBox = new ComboBox<>();
    private final String[] divisionList = {"Dhaka", "Chattogram", "Rajshahi", "Sylhet", "Barisal", "Khulna", "Rangpur",
            "Mymensingh"};
    private final String[] districtList = {"Dhaka","Faridpur","Gazipur","Gopalganj","Jamalpur","Kishoreganj","Madaripur",
            "Manikganj","Munshiganj","Mymensingh","Narayanganj","Narsingdi","Netrokona","Rajbari","Shariatpur","Sherpur","Tangail","Bogra","Joypurhat","Naogaon","Natore","Nawabganj","Pabna","Rajshahi","Sirajgonj","Dinajpur","Gaibandha","Kurigram","Lalmonirhat","Nilphamari","Panchagarh","Rangpur","Thakurgaon","Barguna","Barisal","Bhola","Jhalokati","Patuakhali","Pirojpur","Bandarban","Brahmanbaria","Chandpur","Chittagong","Comilla","Cox''s Bazar","Feni","Khagrachari","Lakshmipur","Noakhali","Rangamati","Habiganj","Maulvibazar","Sunamganj","Sylhet","Bagerhat","Chuadanga","Jessore","Jhenaidah","Khulna","Kushtia","Magura","Meherpur","Narail","Satkhira"};


    public void initialize(URL arg0, ResourceBundle arg1) {
        divisionComboBox.getItems().addAll(divisionList);
        districtComboBox.getItems().addAll(districtList);
        firstNameTextField.setText(User.getInstance().getName().split(" ")[0]);
        lastNameTextField.setText(User.getInstance().getName().split(" ")[1]);

    }
    private boolean isAuthenticate() throws SQLException, ClassNotFoundException {
        String userPhone = User.getInstance().getUserPhoneNo();
        String password = confirmPasswordField.getText();

        if (userPhone != null && !userPhone.isEmpty() && password != null && !password.isEmpty()) {
            return AuthenticationController.authenticateWithPhoneNo(userPhone, password);
        }
        return false;
    }

    @FXML
    public void onClickSaveButton(ActionEvent event) throws SQLException, ClassNotFoundException {
        if (isAuthenticate()){
            // Fix: Use person ID instead of address ID to update address
            int personId = User.getInstance().getUserId();

            // Update person name
            PersonController.updateName(firstNameTextField.getText(), lastNameTextField.getText(), personId);

            // Fix: Use the new person-based address update method
            boolean addressUpdated = AddressController.updateAddressByPersonId(
                personId,
                new Address(divisionComboBox.getValue(), districtComboBox.getValue(), thanaTextField.getText())
            );

            if (addressUpdated) {
                System.out.println("✅ Address updated successfully for user ID: " + personId);
                User.getInstance().setName(firstNameTextField.getText() + " " + lastNameTextField.getText());
                warning.setText("Profile updated successfully!");
                warning.setStyle("-fx-text-fill: green;");
            } else {
                warning.setText("Failed to update address. Please try again.");
                warning.setStyle("-fx-text-fill: red;");
            }
        } else {
            warning.setText("Please enter a valid password");
            warning.setStyle("-fx-text-fill: red;");
        }

        // ...existing navigation code...
    }
}
