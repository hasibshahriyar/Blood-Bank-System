package com.hasib.bloodbank;

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
import com.hasib.bloodbank.utils.ThreadPoolManager;
import com.hasib.bloodbank.utils.NotificationManager;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserProfileController implements Initializable {
    private volatile Reader readerInstance;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();
    private final NotificationManager notificationManager = NotificationManager.getInstance();

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
    NetworkUtility networkUtility;

    // Helper method to safely update warning text
    private void updateWarning(String text, String style) {
        if (warning != null) {
            warning.setText(text);
            if (style != null) {
                warning.setStyle(style);
            }
        } else {
            System.out.println("Warning field is null. Message would be: " + text);
        }
    }

    private void updateWarning(String text) {
        updateWarning(text, null);
    }

    public ShowPerson getShowPerson() {
        return showPerson;
    }

    public void setShowPerson(ShowPerson showPerson) {
        this.showPerson = showPerson;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.showPerson = User.getInstance().getShowPerson();

        // Load person data in background thread
        threadPool.executeDatabaseTask(() -> {
            try {
                getPerson();

                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    updateUI();
                    initializeNetworkConnection();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateWarning("Error loading profile: " + e.getMessage(), "-fx-text-fill: red;");
                });
            }
        });
    }

    private void updateUI() {
        name.setText(showPerson.getName());
        email.setText(showPerson.getEmail());
        mobileNum.setText(showPerson.getPhoneNo());
        bloodGroup.setText(showPerson.getBloodGroup());
        dateOfBirth.setText(person.getDateOfBirth());
        gender.setText(person.getGender());
    }

    void getPerson() {
        try {
            this.person = PersonController.getPersonById(showPerson.getId());
            this.personAddress = AddressController.getAddressById(showPerson.getId());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeNetworkConnection() {
        // Initialize network connection in background
        threadPool.executeNetworkTask(() -> {
            try {
                establishNetworkConnection();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateWarning("Network connection failed: " + e.getMessage(), "-fx-text-fill: orange;");
                });
            }
        });
    }

    private void establishNetworkConnection() throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("www.google.com", 80));
            System.out.println("Client Started--- ");
            System.out.println(socket.getLocalAddress().getHostAddress());

            networkUtility = new NetworkUtility(socket.getLocalAddress().getHostAddress(), 9999);
            networkUtility.write(user.getUserId());

            // Start reader in managed thread
            readerInstance = new Reader(networkUtility, this);
            threadPool.executeNetworkTask(readerInstance);

            isConnected.set(true);

            Platform.runLater(() -> {
                updateWarning("Connected successfully", "-fx-text-fill: green;");
            });

        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            throw e;
        }
    }

    public void handleMessage(String message) {
        Platform.runLater(() -> {
            textArea.appendText("\n" + message);
        });
    }

    public void sendMessage(ActionEvent event) {
        String messageText = textField != null ? textField.getText() : "";
        if (messageText != null && !messageText.trim().isEmpty()) {
            if (isConnected.get() && networkUtility != null && networkUtility.isConnected()) {
                threadPool.executeNetworkTask(() -> {
                    try {
                        Data data = new Data();
                        data.message = user.getUserId() + "$message$" + messageText;
                        networkUtility.write(data.clone());

                        Platform.runLater(() -> {
                            if (textArea != null) textArea.appendText("\nYou: " + messageText);
                            if (textField != null) textField.clear();
                            updateWarning("");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            updateWarning("Failed to send message: " + e.getMessage(), "-fx-text-fill: red;");
                        });
                    }
                });
            } else {
                // Check if networkUtility is initialized
                if (networkUtility == null) {
                    updateWarning("Network connection not ready. Please wait a moment and try again.", "-fx-text-fill: orange;");
                    return;
                }

                threadPool.executeNetworkTask(() -> {
                    try {
                        Data data = new Data();
                        data.message = user.getUserId() + "$message$" + messageText;
                        networkUtility.write(data.clone());

                        Platform.runLater(() -> {
                            if (textArea != null) textArea.appendText("\nYou: " + messageText);
                            if (textField != null) textField.clear();
                            updateWarning("");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            updateWarning("Failed to send message: " + e.getMessage(), "-fx-text-fill: red;");
                        });
                    }
                });
            }
        } else {
            updateWarning("Please enter a message before sending.", "-fx-text-fill: red;");
        }
    }

    public void accept(ActionEvent event) {
        threadPool.executeDatabaseTask(() -> {
            try {
                Platform.runLater(() -> warning.setText(""));

                if (user.getMessage() != null) {
                    String[] message = user.getMessage().split("\\$");
                    DonationController.saveDonation(new Donation(message[3], new Date(new java.util.Date().getTime()),
                            user.getUserId(), showPerson.getId(), BloodGroup.valueOf(user.getBloodGroup())));
                    PersonController.setReadyToDonateFalse(user.getUserId());
                    PersonController.readyToDonateEvent(user.getUserId());

                    Platform.runLater(() -> {
                        warning.setText("Donation accepted successfully");
                        warning.setStyle("-fx-text-fill: green;");
                        hospitalNameTExtFild.setText("");
                        textArea.appendText("\nYou accepted the donation request");
                    });

                    user.setMessage(null);
                } else {
                    Platform.runLater(() -> {
                        warning.setText("No donation request found");
                        warning.setStyle("-fx-text-fill: orange;");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    warning.setText("Error processing donation: " + e.getMessage());
                    warning.setStyle("-fx-text-fill: red;");
                });
            }
        });
    }

    public void request(ActionEvent event) {
        if (!fieldCheck()) {
            return;
        }

        threadPool.executeDatabaseTask(() -> {
            try {
                if (PersonController.getReadyToDonate(showPerson.getId())) {
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
                        // Send socket notification if connected
                        if (isConnected.get() && networkUtility != null && networkUtility.isConnected()) {
                            threadPool.executeNetworkTask(() -> {
                                try {
                                    Data data = new Data();
                                    data.message = user.getUserId() + "$" + showPerson.getId() + "$" +
                                            user.getName() + "$" + "requestForBlood" + "$" + hospitalNameTExtFild.getText();
                                    networkUtility.write(data.clone());
                                } catch (Exception e) {
                                    System.err.println("Socket notification failed: " + e.getMessage());
                                }
                            });
                        }

                        // Add notification using the NotificationManager
                        notificationManager.addNotification(
                                showPerson.getId(),
                                "Blood_Request",
                                "New Blood Request from " + user.getName(),
                                requestMessage,
                                0 // Will be set by the notification manager
                        );

                        Platform.runLater(() -> {
                            updateWarning("Blood Request Sent Successfully!", "-fx-text-fill: green;");
                            if (textArea != null) {
                                textArea.appendText("\n✓ Blood request sent to " + showPerson.getName() +
                                        " for " + hospitalNameTExtFild.getText() + " hospital");
                            }

                            // Show success alert
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Request Sent");
                            alert.setHeaderText("Blood Request Sent Successfully!");
                            alert.setContentText("Your blood request has been sent to " + showPerson.getName() +
                                    ". They will receive a notification and can accept or decline your request.");
                            alert.showAndWait();
                        });

                    } else {
                        Platform.runLater(() -> {
                            updateWarning("Failed to send request. Please try again.", "-fx-text-fill: red;");
                        });
                    }

                } else {
                    Platform.runLater(() -> {
                        updateWarning("This donor is currently not available for donation.", "-fx-text-fill: orange;");
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateWarning("Error sending request: " + e.getMessage(), "-fx-text-fill: red;");
                });
                e.printStackTrace();
            }
        });
    }

    public boolean fieldCheck() {
        if (hospitalNameTExtFild == null || hospitalNameTExtFild.getText() == null || hospitalNameTExtFild.getText().trim().isEmpty()) {
            updateWarning("Please enter hospital name", "-fx-text-fill: red;");
            return false;
        }
        return true;
    }

    // Cleanup method to be called when the controller is destroyed
    public void cleanup() {
        isConnected.set(false);

        if (networkUtility != null) {
            threadPool.executeNetworkTask(() -> {
                networkUtility.closeConnection();
            });
        }

        if (readerInstance != null) {
            readerInstance.stop();
        }
    }

    // Missing method that FXML files are trying to call
    public void onAddressClick(ActionEvent event) {
        try {
            // Show address information in an alert dialog
            String addressInfo = "Address information for " + showPerson.getName();

            // Try to get address from database if available
            if (personAddress != null) {
                addressInfo = "Address: " + personAddress.getDivision() + ", " +
                        personAddress.getDistrict() + ", " + personAddress.getSubDistrict();
            } else {
                addressInfo = "Address information not available";
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Address Information");
            alert.setHeaderText("Address for " + showPerson.getName());
            alert.setContentText(addressInfo);
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("Error showing address: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to show address");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Missing method for the "Send" button in don-user-profile-view.fxml
    public void send(ActionEvent event) {
        try {
            // This appears to be for sending a donation offer message
            String messageText = textField != null ? textField.getText() : "";

            if (messageText != null && !messageText.trim().isEmpty()) {
                sendMessage(event);
            } else {
                // Show donation offer dialog
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Donation Offer");
                alert.setHeaderText("Offer Blood Donation");
                alert.setContentText("Would you like to offer blood donation to " + showPerson.getName() + "?");

                alert.showAndWait().ifPresent(response -> {
                    if (response.getButtonData().isDefaultButton()) {
                        // Send donation offer
                        threadPool.executeNetworkTask(() -> {
                            try {
                                if (isConnected.get() && networkUtility != null && networkUtility.isConnected()) {
                                    Data data = new Data();
                                    data.message = user.getUserId() + "$" + showPerson.getId() + "$" +
                                            user.getName() + "$" + "donateBlood" + "$" + "Blood donation offer";
                                    networkUtility.write(data.clone());

                                    Platform.runLater(() -> {
                                        warning.setText("Donation offer sent successfully!");
                                        warning.setStyle("-fx-text-fill: green;");
                                        textArea.appendText("\n✓ Donation offer sent to " + showPerson.getName());
                                    });
                                }
                            } catch (Exception e) {
                                Platform.runLater(() -> {
                                    warning.setText("Failed to send donation offer: " + e.getMessage());
                                    warning.setStyle("-fx-text-fill: red;");
                                });
                            }
                        });
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("Error in send method: " + e.getMessage());
            warning.setText("Error sending message: " + e.getMessage());
            warning.setStyle("-fx-text-fill: red;");
        }
    }

    // Missing method for the "Click to Back" button
    public void back(ActionEvent event) {
        try {
            // Navigate back to the previous view
            // This could be either donate-blood-view.fxml or request-blood-view.fxml
            // We'll go back to the main menu for safety
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("main.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodBank - Main Menu");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

        } catch (Exception e) {
            System.err.println("Error navigating back: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to go back");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Missing method for the "Reject" button in req-user-profile-view.fxml
    public void reject(ActionEvent event) {
        try {
            // This method handles rejecting a blood donation request
            threadPool.executeDatabaseTask(() -> {
                try {
                    Platform.runLater(() -> warning.setText(""));

                    if (user.getMessage() != null) {
                        // Process rejection of donation request
                        String[] message = user.getMessage().split("\\$");

                        // Send rejection notification back to the requester
                        if (isConnected.get() && networkUtility != null && networkUtility.isConnected()) {
                            threadPool.executeNetworkTask(() -> {
                                try {
                                    Data data = new Data();
                                    data.message = user.getUserId() + "$" + message[0] + "$" +
                                            user.getName() + "$" + "donationRejected" + "$" + "Donation request rejected";
                                    networkUtility.write(data.clone());
                                } catch (Exception e) {
                                    System.err.println("Failed to send rejection notification: " + e.getMessage());
                                }
                            });
                        }

                        Platform.runLater(() -> {
                            warning.setText("Donation request rejected");
                            warning.setStyle("-fx-text-fill: orange;");
                            textArea.appendText("\nYou rejected the donation request");
                        });

                        user.setMessage(null);
                    } else {
                        Platform.runLater(() -> {
                            warning.setText("No donation request to reject");
                            warning.setStyle("-fx-text-fill: orange;");
                        });
                    }

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        warning.setText("Error rejecting donation: " + e.getMessage());
                        warning.setStyle("-fx-text-fill: red;");
                    });
                }
            });

        } catch (Exception e) {
            System.err.println("Error in reject method: " + e.getMessage());
            warning.setText("Error rejecting request: " + e.getMessage());
            warning.setStyle("-fx-text-fill: red;");
        }
    }
}
