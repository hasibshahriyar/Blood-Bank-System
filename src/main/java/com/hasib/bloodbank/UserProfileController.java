package com.hasib.bloodbank;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.hasib.bloodbank.server.controller.AddressController;
import com.hasib.bloodbank.server.controller.BloodRequestController;
import com.hasib.bloodbank.server.controller.DonationController;
import com.hasib.bloodbank.server.controller.PersonController;
import com.hasib.bloodbank.server.entity.Address;
import com.hasib.bloodbank.server.entity.BloodGroup;
import com.hasib.bloodbank.server.entity.Donation;
import com.hasib.bloodbank.server.entity.Person;
import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.singleton.User;
import com.hasib.bloodbank.socketclient.Reader;
import com.hasib.bloodbank.utils.Data;
import com.hasib.bloodbank.utils.NetworkUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable, Runnable {
    public Thread readerThread;
    public JFXTextArea textArea;
    public Label addressLabel;
    public Label email;
    public Label dateOfBirth;
    public Label mobileNum;
    public Label gender;
    public Label bloodGroup;
    public Label name;
    public TextField hospitalNameTExtFild;
    public Text warning;
    public TextField textField;
    User user = User.getInstance();
    ShowPerson showPerson;
    String gen;
    Person person;
    Address personAddress;

    public ShowPerson getShowPerson() {
        return showPerson;
    }

    public void setShowPerson(ShowPerson showPerson) {
        this.showPerson = showPerson;
    }

    NetworkUtility networkUtility;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.showPerson=User.getInstance().getShowPerson();
        getPerson();
        name.setText(showPerson.getName());
        email.setText(showPerson.getEmail());
        mobileNum.setText(showPerson.getPhoneNo());
        bloodGroup.setText(showPerson.getBloodGroup());
        dateOfBirth.setText(person.getDateOfBirth());
        gender.setText(person.getGender());

        new Thread(this).start();

    }
    void getPerson(){
        try {
            this.person=PersonController.getPersonById(showPerson.getId());
            this.personAddress= AddressController.getAddressById(showPerson.getId());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("www.google.com", 80));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client Started--- ");
        System.out.println(socket.getLocalAddress().getHostAddress());

        try {
            networkUtility = new NetworkUtility(socket.getLocalAddress().getHostAddress(), 9999);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        networkUtility.write(user.getUserId());

        readerThread = new Thread(new Reader(networkUtility, this));

        readerThread.start();
        try {
            readerThread.join();
        } catch (Exception e) {
            System.out.println("Thread exited");
        }
    }

    //    words[0] = Sender Id
//    words[1] = Receiver Id
//    words[2] = Sender Name
//    words[3] = keyword
//    words[4] = message/null
    public void onAddressClick(ActionEvent event) {
        addressLabel.setText(personAddress.getCountry() + ", " + personAddress.getDivision() + ", " + personAddress.getDistrict() + ", " + personAddress.getSubDistrict() + ".");

    }

    public void send(ActionEvent event) throws CloneNotSupportedException {
        if (!textField.getText().isBlank()) {
            // Check if networkUtility is initialized
            if (networkUtility == null) {
                warning.setText("Network connection not ready. Please wait a moment and try again.");
                warning.setStyle("-fx-text-fill: orange;");
                return;
            }

            try {
                Data data = new Data();
                data.message = user.getUserId() + "$" + showPerson.getId() + "$" + user.getName() + "$" + "text" + "$" + textField.getText();
                networkUtility.write(data.clone());
                textArea.appendText("\n"+textField.getText() + "\n");
                textField.clear();
                warning.setText("Message sent successfully!");
                warning.setStyle("-fx-text-fill: green;");
            } catch (Exception e) {
                warning.setText("Failed to send message: " + e.getMessage());
                warning.setStyle("-fx-text-fill: red;");
            }
        } else {
            warning.setText("Please enter a message before sending.");
            warning.setStyle("-fx-text-fill: red;");
        }
    }

    public void accept(ActionEvent event) throws SQLException, ClassNotFoundException {
        warning.setText("");
        if (user.getMessage() != null) {
            String[] message = user.getMessage().split("\\$");
            DonationController.saveDonation(new Donation(message[3], new Date(new java.util.Date().getTime()), user.getUserId(), showPerson.getId(), BloodGroup.valueOf(user.getBloodGroup())));
            PersonController.setReadyToDonateFalse(user.getUserId());
            PersonController.readyToDonateEvent( user.getUserId());
            warning.setText("donation done");
            hospitalNameTExtFild.setText("");
            textArea.appendText("\nyou accept donation request");
            user.setMessage(null);
        }else warning.setText("no donation request found");

    }

    public void request(ActionEvent event) throws SQLException, ClassNotFoundException, CloneNotSupportedException {
        if (fieldCheck()) {
            if (PersonController.getReadyToDonate(showPerson.getId())) {
                try {
                    // Create database record for the blood request
                    String requestMessage = "Blood request for " + hospitalNameTExtFild.getText() +
                            " hospital. Requester: " + user.getName() +
                            " (" + user.getBloodGroup() + " blood group needed)";

                    boolean requestSent = BloodRequestController.sendBloodRequest(
                            user.getUserId(),
                            showPerson.getId(),
                            requestMessage
                    );

                    if (requestSent) {
                        // Only send socket message if networkUtility is available (optional for backwards compatibility)
                        if (networkUtility != null) {
                            try {
                                Data data = new Data();
                                data.message = user.getUserId() + "$" + showPerson.getId() + "$" + user.getName() + "$" + "requestForBlood" + "$" + hospitalNameTExtFild.getText();
                                networkUtility.write(data.clone());
                            } catch (Exception e) {
                                // Log but don't fail the request if socket fails
                                System.err.println("Socket notification failed: " + e.getMessage());
                            }
                        }

                        warning.setText("Blood Request Sent Successfully!");
                        warning.setStyle("-fx-text-fill: green;");
                        textArea.appendText("\n✓ Blood request sent to " + showPerson.getName() + " for " + hospitalNameTExtFild.getText() + " hospital");

                        // Show success alert
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Request Sent");
                        alert.setHeaderText("Blood Request Sent Successfully!");
                        alert.setContentText("Your blood request has been sent to " + showPerson.getName() +
                                ". They will receive a notification and can accept or decline your request.");
                        alert.showAndWait();

                    } else {
                        warning.setText("Failed to send request. Please try again.");
                        warning.setStyle("-fx-text-fill: red;");
                    }

                } catch (Exception e) {
                    warning.setText("Error sending request: " + e.getMessage());
                    warning.setStyle("-fx-text-fill: red;");
                }

            } else {
                warning.setText("This person is not available for blood donation at the moment.");
                warning.setStyle("-fx-text-fill: orange;");
            }
            hospitalNameTExtFild.setText("");
        } else {
            warning.setText("Please enter the hospital name.");
            warning.setStyle("-fx-text-fill: red;");
        }
    }

    private boolean fieldCheck() {
        return  !hospitalNameTExtFild.getText().isBlank();
    }

    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("BloodBank");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void reject(ActionEvent event) {
    }
}
