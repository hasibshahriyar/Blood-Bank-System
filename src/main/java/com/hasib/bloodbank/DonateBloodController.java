package com.hasib.bloodbank;

import com.hasib.bloodbank.server.controller.PersonController;
import com.hasib.bloodbank.server.entity.Person;
import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.singleton.User;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class DonateBloodController implements Initializable {

    public ComboBox<String> bloodGroupComboBox;
    public ComboBox<String> divisionComboBox;
    public ComboBox<String> districtComboBox;
    public List<Person> personList;
    public TableColumn<ShowPerson,String> bloodgroupColumn;
    public TableColumn<ShowPerson,String> phonenumberColumn;
    public TableColumn<ShowPerson,String> nameColumn;
    public TableColumn<ShowPerson,String> emailColumn;
    public TableView<ShowPerson> tableView;
    public TableColumn<ShowPerson,String> id;
    public Label availableRecipientsLabel;
    public ShowPerson person;
    ObservableList<ShowPerson> personObservableList;

    public void onActionLoadPeople(ActionEvent event) throws SQLException, ClassNotFoundException {
        personObservableList = PersonController.getPersonWhoNeedBlood2();
        loadTableData();
        updateAvailableRecipientsCount();
    }

    public void onClickFilterByBloodGroup(ActionEvent event) throws SQLException, ClassNotFoundException {
        String selectedBloodGroup = bloodGroupComboBox.getValue();
        if (selectedBloodGroup != null && !selectedBloodGroup.isEmpty()) {
            personObservableList = PersonController.getRecipientsByBloodGroup(selectedBloodGroup);
            loadTableData();
            updateAvailableRecipientsCount();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Blood Group Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a blood group to filter recipients.");
            alert.showAndWait();
        }
    }

    public void onClickFilterByLocation(ActionEvent event) throws SQLException, ClassNotFoundException {
        String selectedDivision = divisionComboBox.getValue();
        String selectedDistrict = districtComboBox.getValue();

        if (selectedDivision != null && selectedDistrict != null &&
                !selectedDivision.isEmpty() && !selectedDistrict.isEmpty()) {
            personObservableList = PersonController.getRecipientsByLocation(selectedDivision, selectedDistrict);
            loadTableData();
            updateAvailableRecipientsCount();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Location Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select both division and district to filter by location.");
            alert.showAndWait();
        }
    }

    public void onClickClearFilters(ActionEvent event) throws SQLException, ClassNotFoundException {
        bloodGroupComboBox.setValue(null);
        divisionComboBox.setValue(null);
        districtComboBox.setValue(null);
        onActionLoadPeople(event);
    }

    private void loadTableData() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phonenumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNo"));
        bloodgroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableView.setItems(personObservableList);
    }

    private void updateAvailableRecipientsCount() {
        int count = personObservableList != null ? personObservableList.size() : 0;
        if (count == 0) {
            availableRecipientsLabel.setText("😌 No one needs help right now - what a blessing!");
            availableRecipientsLabel.setStyle("-fx-text-fill: #4ECDC4; -fx-font-weight: bold;");
        } else if (count == 1) {
            availableRecipientsLabel.setText("💙 1 person is waiting for your kindness");
            availableRecipientsLabel.setStyle("-fx-text-fill: #A60000; -fx-font-weight: bold;");
        } else {
            availableRecipientsLabel.setText("🙏 " + count + " brave souls need your help - you can be their hero");
            availableRecipientsLabel.setStyle("-fx-text-fill: #A60000; -fx-font-weight: bold;");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize blood group combo box
        bloodGroupComboBox.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

        // Initialize division combo box
        divisionComboBox.getItems().addAll("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet", "Rangpur", "Mymensingh");

        // Initialize district combo box
        districtComboBox.getItems().addAll("Dhaka", "Gazipur", "Narayanganj", "Chittagong", "Cox's Bazar", "Rajshahi", "Khulna", "Sylhet");
    }

    public void omMouseClick(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 1) {
            person = tableView.getSelectionModel().getSelectedItem();
            User.getInstance().setShowPerson(person);
        }
        if (mouseEvent.getClickCount() == 2) {
            if (person != null) {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("don-user-profile-view.fxml")));
                Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
                stage.setTitle("BloodBank - Recipient Profile");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Recipient Selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select a recipient to view their profile.");
                alert.showAndWait();
            }
        }
    }
}
