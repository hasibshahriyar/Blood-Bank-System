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

    public static boolean updateAddressByPersonId(int personId, Address address) {
        Connection connection = null;
        boolean isUpdated = false;
        try {
            connection = ConnectionProvider.createConnection();

            // First check if an address exists for this person
            String checkQuery = "SELECT id FROM address WHERE person_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, personId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Address exists, update it - FIXED: Use correct column name 'thana'
                String updateQuery = "UPDATE address SET division = ?, district = ?, thana = ? WHERE person_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setString(1, address.getDivision());
                updateStmt.setString(2, address.getDistrict());
                updateStmt.setString(3, address.getSubDistrict()); // getSubDistrict() maps to 'thana' column
                updateStmt.setInt(4, personId);
                updateStmt.executeUpdate();
                isUpdated = true;
            } else {
                // No address exists, create a new one - FIXED: Column mapping
                String insertQuery = "INSERT INTO address (person_id, division, district, thana) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setInt(1, personId);
                insertStmt.setString(2, address.getDivision());
                insertStmt.setString(3, address.getDistrict());
                insertStmt.setString(4, address.getSubDistrict()); // getSubDistrict() maps to 'thana' column
                insertStmt.executeUpdate();
                isUpdated = true;
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error updating address for person ID " + personId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        return isUpdated;
    }

    // Keep the old method for backward compatibility but mark it as deprecated
    @Deprecated
    public static boolean updateAddress(int id, Address address) {
        System.err.println("WARNING: updateAddress(int id, Address address) is deprecated. Use updateAddressByPersonId() instead.");
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

    public static Address getAddressByPersonId(int personId) throws SQLException, ClassNotFoundException {
        Address address = null;
        Connection connection = ConnectionProvider.createConnection();

        // Use parameterized query to prevent SQL injection
        String query = "SELECT * FROM address WHERE person_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, personId);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String district = resultSet.getString("district");
            String subDistrict = resultSet.getString("thana");
            String division = resultSet.getString("division");
            address = new Address(division, district, subDistrict);
        }

        connection.close();
        return address;
    }
}
