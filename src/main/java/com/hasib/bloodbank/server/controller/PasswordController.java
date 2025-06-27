package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.server.entity.Password;
import com.hasib.bloodbank.server.provider.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PasswordController {

    public static boolean savePassword(Password password) throws SQLException, ClassNotFoundException {

        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "insert into password(password) values('" + password.getPassword() + "');";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
        int rowsAffected = preparedStatement.executeUpdate();
        connection.close();
        return rowsAffected > 0;  // Return true if at least one row was inserted

    }

    public static boolean updatePassword(int id, String password) {
        Connection connection = null;
        try {
            connection = ConnectionProvider.createConnection();
            String addressQuarry = "UPDATE password SET password.password = '" + password + "' WHERE password.id='" + id + "';";
            PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
            preparedStatement.execute();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static boolean deletePassword(int id) {
        Connection connection = null;
        boolean isDeleted = false;
        try {
            connection = ConnectionProvider.createConnection();
            String addressQuarry = "DELETE from password WHERE password.id='" + id + "';";
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


}
