package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.server.entity.Address;
import com.hasib.bloodbank.server.provider.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressController {
    //    public static int saveAddress(Address address) throws SQLException, ClassNotFoundException {
//
//        int addressId = 0;
//        Connection connection = ConnectionProvider.createConnection();
//        String addressQuarry = "insert  into  address(country, district, division, sub_district)   values('" + address.getCountry() + "','" + address.getDivision() + "','" + address.getDistrict() + "','" + address.getSubDistrict() + "');";
//        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry, Statement.RETURN_GENERATED_KEYS);
//        preparedStatement.executeUpdate();
//        ResultSet resultSe = preparedStatement.getGeneratedKeys();
//
//        while (resultSe.next()){
//            addressId=resultSe.getInt(1);
//        }
//
//        return addressId;
//    }
    public static void saveAddress(Address address) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "insert  into  address(division, district, thana)   values('" + address.getDivision() + "','" + address.getDistrict() + "','" + address.getSubDistrict() + "');";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
        boolean a = preparedStatement.execute();
        connection.close();

    }

    public static boolean updateAddress(int id, Address address) {
        Connection connection = null;
        boolean isUpdated = false;
        try {
            connection = ConnectionProvider.createConnection();
            String addressQuarry = "UPDATE address SET address.district = '" + address.getDistrict() + "',address.division = '" + address.getDivision() + "',address.thana = '" + address.getSubDistrict() + "' WHERE address.id='" + id + "';";
            PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
            preparedStatement.execute();
            isUpdated = true;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return isUpdated;
    }

    public static boolean deleteAddress(int id) {
        Connection connection = null;
        boolean isDeleted = false;
        try {
            connection = ConnectionProvider.createConnection();
            String addressQuarry = "DELETE from address WHERE address.id='" + id + "';";
            PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
            preparedStatement.execute();
            isDeleted = true;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return isDeleted;
    }
    public static Address getAddressById(int id) throws SQLException, ClassNotFoundException {


        String district;
        String subDistrict;
        String division;

        Address address = null;
        Connection connection = ConnectionProvider.createConnection();
        String quarry = "SELECT  * FROM address WHERE id='"+id+"';";
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            district=resultSet.getString("district");
            subDistrict = resultSet.getString("thana");
            division=resultSet.getString("division");
            address=new Address(division,district,subDistrict);


        }
        return address ;
    }

}
