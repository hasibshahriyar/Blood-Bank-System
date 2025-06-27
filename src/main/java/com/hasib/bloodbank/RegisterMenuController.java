package com.hasib.bloodbank;

import com.hasib.bloodbank.server.controller.AddressController;
import com.hasib.bloodbank.server.controller.PasswordController;
import com.hasib.bloodbank.server.controller.PersonController;
import com.hasib.bloodbank.server.entity.*;
import com.hasib.bloodbank.server.model.Person;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterMenuController implements Initializable {
    private final Person p = Person.getInstanceOfModelPerson();
    public Text firstNameWarning;
    public Text lastNameWarning;
    public Text genderWarning;
    public Text bloodGroupWarning;
    public Text dateOfBirthWarning;
    public Text emailWarning;
    public Text mobileWarning;
    public Text passwordWarning;
    public Text addressWarning;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private ComboBox<String> bloodGroupComboBox = new ComboBox<>();
    @FXML
    private ComboBox<String> genderComboBox = new ComboBox<>();
    private final String[] bloodGroupsList = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
    private final String[] genderList = {"Male", "Female", "Other"};
    @FXML
    private DatePicker dateOfBirthDatePicker;
    @FXML
    private Button cancelButton;
    @FXML
    private Parent root;
    @FXML
    private Stage registerStage;
    //
    public TextField thanaTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField mobileNumTextField;
    @FXML
    private TextField nidNumTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> divisionComboBox = new ComboBox<>();
    private final String[] divisionList = {"Dhaka", "Chattogram", "Rajshahi", "Sylhet", "Barisal", "Khulna", "Rangpur",
            "Mymensingh"};
    @FXML
    private ComboBox<String> districtComboBox = new ComboBox<>();
    private final String[] districtList = {"Dhaka","Faridpur","Gazipur","Gopalganj","Jamalpur","Kishoreganj","Madaripur",
            "Manikganj","Munshiganj","Mymensingh","Narayanganj","Narsingdi","Netrokona","Rajbari","Shariatpur","Sherpur","Tangail","Bogra","Joypurhat","Naogaon","Natore","Nawabganj","Pabna","Rajshahi","Sirajgonj","Dinajpur","Gaibandha","Kurigram","Lalmonirhat","Nilphamari","Panchagarh","Rangpur","Thakurgaon","Barguna","Barisal","Bhola","Jhalokati","Patuakhali","Pirojpur","Bandarban","Brahmanbaria","Chandpur","Chittagong","Comilla","Cox''s Bazar","Feni","Khagrachari","Lakshmipur","Noakhali","Rangamati","Habiganj","Maulvibazar","Sunamganj","Sylhet","Bagerhat","Chuadanga","Jessore","Jhenaidah","Khulna","Kushtia","Magura","Meherpur","Narail","Satkhira"};
    @FXML
    private Button registerButton;
    private String password;
    private String division;
    private String district;
    private String thana;
    final String  emailRegex = "^(.+)@(.+)$";
    final String phoneRegex = "^01[13-9]\\d{8}$";
    Pattern pattern;
    Matcher matcher;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bloodGroupComboBox.getItems().addAll(bloodGroupsList);
        genderComboBox.getItems().addAll(genderList);
        divisionComboBox.getItems().addAll(divisionList);
        districtComboBox.getItems().addAll(districtList);
    }

    @FXML
    protected void onClickCancelButton(ActionEvent event){
        try {
            if (fieldCheck()){
                takingInput();
                nextPage("login-view.fxml", event);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void nextPage(String name, ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(name)));
        registerStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        registerStage.setScene(new Scene(root));
        registerStage.show();
    }

    private boolean fieldCheck() {
        if (firstNameTextField.getText().isBlank()){
            firstNameWarning.setText("first name can't be empty");
            return false;
        }
        if (lastNameTextField.getText().isBlank()){
            lastNameWarning.setText("last name can't be empty");
            return false;
        }

        if (genderComboBox.getValue()==null){
            genderWarning.setText("gender can't be empty");
            return false;
        }
        if (bloodGroupComboBox.getValue()==null){
            bloodGroupWarning.setText("blood group can't be empty");
            return false;
        }
        if (dateOfBirthDatePicker.getValue()==null){
            dateOfBirthWarning.setText("birth date can't be empty");
            return false;
        }
        if (emailTextField.getText().isBlank()){
            emailWarning.setText("email required");
            return false;
        }
        if (mobileNumTextField.getText().isBlank()){
            mobileWarning.setText("mobile no required");
            return false;
        }
        if (passwordField.getText().isEmpty()){
            passwordWarning.setText("password required");
            return false;
        }
        if (divisionComboBox.getValue()==null){
            addressWarning.setText("division required");
            return false;
        } else if (districtComboBox.getValue()==null){
            addressWarning.setText("district required");
            return false;
        } else if (thanaTextField.getText().isBlank()) {
            addressWarning.setText("thana required");
            return false;
        }
        if (!checkIsValidEmail()) {
            emailWarning.setText("invalid email");
            return false;
        }
        if (!checkIsValidPhone()) {
            mobileNumTextField.setText("invalid phone no");
            return false;
        }
        return true;
    }


    private void takingInput(){
        p.setDateOfBirth(String.valueOf(dateOfBirthDatePicker.getValue()));
        p.setFirstName(firstNameTextField.getText());
        p.setLastName(lastNameTextField.getText());
        p.setPhoneNumber(mobileNumTextField.getText());
        p.setEmail(emailTextField.getText());
        password = passwordField.getText();
        district=districtComboBox.getValue();
        thana=thanaTextField.getText();
        division=divisionComboBox.getValue();
    }

    @FXML
    protected void onClickRegisterButton(ActionEvent event){
        if (fieldCheck()){
            takingInput();

            try {
                // Step 1: Save password first
                Password passwordEntity = new Password(password);
                boolean passwordSaved = PasswordController.savePassword(passwordEntity);
                if (!passwordSaved) {
                    addressWarning.setText("Failed to save password. Please try again.");
                    return;
                }

                // Step 2: Save person (the PersonController doesn't return boolean)
                PersonController.savePerson(
                        p.getFirstName(),
                        p.getLastName(),
                        p.getPhoneNumber(),
                        p.getEmail(),
                        p.getDateOfBirth(),
                        p.getBloodGroup(),
                        p.getGender()
                );

                // Step 3: Save address (the AddressController doesn't return boolean)
                Address addressEntity = new Address(division, district, thana);
                AddressController.saveAddress(addressEntity);

                // Step 4: Navigate to login page
                nextPage("login-view.fxml", event);

            } catch (SQLException | ClassNotFoundException | IOException e) {
                System.err.println("Registration failed: " + e.getMessage());
                e.printStackTrace();
                // Show error message to user
                addressWarning.setText("Registration failed. Please try again.");
            }
        }
    }

    public void onClickBloodGroupComboBox(ActionEvent event) {
        bloodGroupWarning.setText("");
        String Group = bloodGroupComboBox.getValue();

        switch(Group) {
            case "O+":
                p.setBloodGroup(BloodGroup.O_POSITIVE);
                break;
            case "O-":
                p.setBloodGroup(BloodGroup.O_NEGATIVE);

                break;
            case "A+":
                p.setBloodGroup(BloodGroup.A_POSITIVE);

                break;
            case "A-":
                p.setBloodGroup(BloodGroup.A_NEGATIVE);

                break;
            case "B+":
                p.setBloodGroup(BloodGroup.B_POSITIVE);

                break;
            case "B-":
                p.setBloodGroup(BloodGroup.B_NEGATIVE);

                break;
            case "AB+":
                p.setBloodGroup(BloodGroup.AB_POSITIVE);

                break;
            case "AB-":
                p.setBloodGroup(BloodGroup.AB_NEGATIVE);

                break;

        }
    }

    public void onClickGenderComboBox(ActionEvent event) {
        genderWarning.setText("");
        String genderA = genderComboBox.getValue();
        switch(genderA) {
            case "Male":
                p.setGender(Gender.MALE);
                break;
            case "Female":
                p.setGender(Gender.FEMALE);
                break;
            case "Other":
                p.setGender(Gender.OTHER);
                break;
        }

    }


    public void onClickDateOfBirthDatePicker(ActionEvent event) {
        dateOfBirthWarning.setText("");
        dateOfBirthDatePicker.setEditable(false);

    }


    public void onKeyReleasedFirstNameTextField(KeyEvent keyEvent) {
        firstNameWarning.setText("");
    }

    public void onKeyReleasedLastNameTextField(KeyEvent keyEvent) {
        lastNameWarning.setText("");
    }

    public void onKeyReleaseEmailInputValidation(KeyEvent keyEvent) {

        if (!checkIsValidEmail()){
            emailWarning.setText("invalid Email");
        }else emailWarning.setText("");
    }

    public void onKeyReleaseMobileTextField(KeyEvent keyEvent) {
        if (!checkIsValidPhone()){
            mobileWarning.setText("invalid Phone No");
        }else mobileWarning.setText("");

    }



    private boolean checkIsValidEmail(){
        return Pattern.compile(emailRegex).matcher(emailTextField.getText()).matches();
    }
    private boolean checkIsValidPhone(){
        return Pattern.compile(phoneRegex).matcher(mobileNumTextField.getText()).matches();
    }

    public void onClickDivision(ActionEvent event) {
        addressWarning.setText("");

    }

    public void onClickDistrict(ActionEvent event) {
        addressWarning.setText("");
    }

    public void onClickThana(ActionEvent event) {
        addressWarning.setText("");
    }

    public void onLoginButtonClick(ActionEvent event) throws IOException {

        nextPage("login-view.fxml",event);
    }

}
