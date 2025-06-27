package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.server.entity.Donation;
import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.server.provider.ConnectionProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DonationController {
    public static int saveDonation(Donation donation) throws SQLException, ClassNotFoundException {

        int donationId = 0;
        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "insert  into  donation(blood_group,donation_date, hospital_name, received_donner_id, given_donner_id)   values('"+donation.getBloodGroup()+"','" + donation.getDonationDate() + "','" + donation.getHospitalName() + "','" + donation.getReceivedPersonId() + "','" + donation.getDonatedPersonId() + "');";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.executeUpdate();
        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        System.out.println(donation.getBloodGroup());

        while (resultSet.next()) {
            donationId = resultSet.getInt(1);
        }

        return donationId;
    }
    public static ObservableList<Donation> getReceivedDonationById(int id) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT donation.donation_date,donation.hospital_name FROM person INNER JOIN donation ON donation.received_donner_id = person.id where person.id="+ id+";" ;
        return getDonationObservableList(quarry);
    }

    private static ObservableList<Donation> getDonationObservableList(String quarry) throws ClassNotFoundException, SQLException {
        Date date;
        String hospitalName;
        ObservableList<Donation> list = FXCollections.observableArrayList();
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            date=resultSet.getDate(1);
            hospitalName=resultSet.getString(2);
            list.add(new Donation(hospitalName,date,0,0,null));
        }
        return list;
    }

    public static ObservableList<Donation> getDonatedDonationById(int id) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT donation.donation_date,donation.hospital_name FROM person INNER JOIN donation ON donation.given_donner_id = person.id where person.id="+ id+";" ;
        return getDonationObservableList(quarry);
    }

}
