package com.hasib.bloodbank;

import com.hasib.bloodbank.server.controller.AddressController;
import com.hasib.bloodbank.server.controller.DonationController;
import com.hasib.bloodbank.server.controller.PersonController;
import com.hasib.bloodbank.server.entity.Address;
import com.hasib.bloodbank.server.entity.Donation;
import com.hasib.bloodbank.server.entity.Person;
import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.singleton.User;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    public Label address;
    public Label email;
    public Label mobileNum;
    public Label bloodGroup;
    public Label name;
    public Label gender;
    public Label dateOfBirth;
    public TableColumn<Donation,String> hospitalColumn;
    public TableColumn<Donation, Date> dateColumn;
    public TableView<Donation> table;
    public CheckBox donorAvailabilityCheckBox;
    public Label donorStatusLabel;
    public CheckBox recipientStatusCheckBox;
    public Label recipientStatusLabel;

    User user = User.getInstance();
    Person person;
    Address personAddress;
    ObservableList<Donation> donationObservableList;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            person = PersonController.getPersonById(user.getUserId());

            // Initialize donor availability status
            boolean isDonorAvailable = PersonController.getDonorAvailabilityStatus(user.getUserId());
            donorAvailabilityCheckBox.setSelected(isDonorAvailable);
            updateDonorStatusLabel(isDonorAvailable);

            // Initialize recipient status
            boolean needsBlood = PersonController.getNeedBloodStatus(user.getUserId());
            recipientStatusCheckBox.setSelected(needsBlood);
            updateRecipientStatusLabel(needsBlood);

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        name.setText(person.getFirstName() + " " + person.getLastName());
        bloodGroup.setText(person.getBloodGroup());
        gender.setText(person.getGender());
        dateOfBirth.setText(person.getDateOfBirth());
        email.setText(person.getEmail());
        mobileNum.setText(person.getPhoneNumber());


    }

    public void onAddressClick(ActionEvent event) {
        try {
            personAddress = AddressController.getAddressById(person.getAddressId());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        address.setText(personAddress.getCountry() + ", " + personAddress.getDivision() + ", " + personAddress.getDistrict() + ", " + personAddress.getSubDistrict() + ".");

    }

    public void donationData(ActionEvent event) throws SQLException, ClassNotFoundException {
        hospitalColumn.setCellValueFactory(new PropertyValueFactory<>("hospitalName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
        donationObservableList=DonationController.getDonatedDonationById(User.getInstance().getUserId());
        table.setItems(donationObservableList);
    }

    public void receivedData(ActionEvent event) throws SQLException, ClassNotFoundException {
        hospitalColumn.setCellValueFactory(new PropertyValueFactory<>("hospitalName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
        donationObservableList=DonationController.getReceivedDonationById(User.getInstance().getUserId());
        table.setItems(donationObservableList);

    }

    public void onDonorAvailabilityToggle(ActionEvent event) {
        try {
            boolean isSelected = donorAvailabilityCheckBox.isSelected();
            PersonController.setReadyToDonate(user.getUserId(), isSelected);
            updateDonorStatusLabel(isSelected);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Donor Status Updated");
            alert.setHeaderText(null);
            alert.setContentText("Your donor availability status has been " +
                    (isSelected ? "enabled" : "disabled") + " successfully!");
            alert.showAndWait();

        } catch (SQLException | ClassNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update donor status. Please try again.");
            alert.showAndWait();
        }
    }

    public void onRecipientStatusToggle(ActionEvent event) {
        try {
            boolean isSelected = recipientStatusCheckBox.isSelected();
            PersonController.setNeedBlood(user.getUserId(), isSelected);
            updateRecipientStatusLabel(isSelected);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Recipient Status Updated");
            alert.setHeaderText(null);
            alert.setContentText("Your blood recipient status has been " +
                    (isSelected ? "enabled" : "disabled") + " successfully!");
            alert.showAndWait();

        } catch (SQLException | ClassNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update recipient status. Please try again.");
            alert.showAndWait();
        }
    }

    private void updateDonorStatusLabel(boolean isAvailable) {
        if (isAvailable) {
            donorStatusLabel.setText("Status: Available for Blood Donation");
            donorStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            donorStatusLabel.setText("Status: Not Available for Blood Donation");
            donorStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void updateRecipientStatusLabel(boolean needsBlood) {
        if (needsBlood) {
            recipientStatusLabel.setText("Status: Needs Blood");
            recipientStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            recipientStatusLabel.setText("Status: Does Not Need Blood");
            recipientStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
    }
}
