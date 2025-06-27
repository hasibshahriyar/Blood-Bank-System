package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.singleton.User;
import com.hasib.bloodbank.server.provider.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationController {
    public static boolean authenticateWithPhoneNo(String phone, String password) throws ClassNotFoundException, SQLException {
        int personId = 0;
        String passwordFromDb = null;
        String phoneNoFromDb = null;
        String emailFromDb = null;
        String parsonName = null;
        String bloodGroup = null;
        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "SELECT person.id ,person.first_name,person.last_name, person.phone_number , person.email ,person.blood_group, password.id , password.password FROM person RIGHT JOIN password ON person.password_id=password.id WHERE phone_number='" + phone + "';";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
        preparedStatement.executeQuery();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            personId = resultSet.getInt(1);
            parsonName = resultSet.getString(2) + " " + resultSet.getString(3);
            phoneNoFromDb = resultSet.getString(4);
            emailFromDb = resultSet.getString(5);
            bloodGroup = resultSet.getString(6);
            passwordFromDb = resultSet.getString(8);
        }

        if (password.equals(passwordFromDb) && phone.equals(phoneNoFromDb)) {
            User.getInstance().setUserId(personId);
            User.getInstance().setUserPhoneNo(phoneNoFromDb);
            User.getInstance().setUserEmail(emailFromDb);
            User.getInstance().setName(parsonName);
            User.getInstance().setBloodGroup(bloodGroup);
            return true;
        }

        return false;
    }

    public static boolean authenticateWithEmail(String email, String password) throws ClassNotFoundException, SQLException {
        int personId = 0;
        String passwordFromDb = null;
        String phoneNoFromDb = null;
        String emailFromDb = null;
        String parsonName = null;
        String bloodGroup = null;
        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "SELECT person.id ,person.first_name,person.last_name, person.phone_number , person.email ,person.blood_group, password.id , password.password FROM person RIGHT JOIN password ON person.password_id=password.id WHERE email='" + email + "';";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
        preparedStatement.executeQuery();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            personId = resultSet.getInt(1);
            parsonName = resultSet.getString(2) + " " + resultSet.getString(3);
            phoneNoFromDb = resultSet.getString(4);
            emailFromDb = resultSet.getString(5);
            bloodGroup = resultSet.getString(6);
            passwordFromDb = resultSet.getString(8);

        }

        if (password.equals(passwordFromDb) && email.equals(emailFromDb)) {
            User.getInstance().setUserId(personId);
            User.getInstance().setUserPhoneNo(phoneNoFromDb);
            User.getInstance().setUserEmail(emailFromDb);
            User.getInstance().setName(parsonName);
            User.getInstance().setBloodGroup(bloodGroup);
            System.out.println(bloodGroup + "from authentication controller");
            return true;
        }

        return false;
    }

    public static boolean isAuthentic(String phone, String password) throws ClassNotFoundException, SQLException {
        String pas = null;
        String phoneNO = null;
        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "SELECT person.id,person.phone_number , person.email , password.id , password.password FROM person RIGHT JOIN password ON person.password_id=password.id WHERE phone_number='" + phone + "';";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
        preparedStatement.executeQuery();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            phoneNO = resultSet.getString(2);
            pas = resultSet.getString(5);
        }
        System.out.println(pas);
        System.out.println(phoneNO);


        return password.equals(pas) && phone.equals(phoneNO);
    }

    public static boolean changePassword(String phone, String password) throws ClassNotFoundException, SQLException {
        int passwordId = isExistPhone(phone);
        if (passwordId > 0) {
            return PasswordController.updatePassword(passwordId, password);
        }
        return false;
    }

    public static int isExistPhone(String phone) throws ClassNotFoundException, SQLException {
        int passwordId = 0;
        Connection connection = ConnectionProvider.createConnection();
        String personQuarry = "SELECT * From person WHERE  phone_number ='" + phone + "';";
        PreparedStatement preparedStatement = connection.prepareStatement(personQuarry);
        preparedStatement.executeQuery();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            passwordId = resultSet.getInt(12);
        }


        return passwordId;
    }
    public static int isExistEmail(String email) throws ClassNotFoundException, SQLException {
        int passwordId = 0;
        Connection connection = ConnectionProvider.createConnection();
        String personQuarry = "SELECT * From person WHERE  email ='" + email + "';";
        PreparedStatement preparedStatement = connection.prepareStatement(personQuarry);
        preparedStatement.executeQuery();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            passwordId = resultSet.getInt(12);
        }


        return passwordId;
    }
}
