package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.server.entity.BloodGroup;
import com.hasib.bloodbank.server.entity.Gender;
import com.hasib.bloodbank.server.entity.Person;
import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.server.provider.ConnectionProvider;
import com.hasib.bloodbank.singleton.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PersonController {

    public static void savePerson(String firstName, String lastName, String phoneNo, String email, String dateOfBirth, BloodGroup bloodGroup, Gender gender) throws SQLException, ClassNotFoundException {

        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "INSERT INTO person(first_name, last_name, phone_number, email, blood_group, gender, date_of_birth, password_id, ready_to_donate, need_blood, address_id) VALUES('" + firstName + "','" + lastName + "','" + phoneNo + "','" + email + "','" + bloodGroup + "','" + gender + "','" + dateOfBirth + "',(SELECT MAX(id) FROM password), 1, 0, (SELECT MAX(id) FROM address))";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
        preparedStatement.execute();
        connection.close();
    }
    public static void updateName(String firstName, String lastName, int id) throws SQLException, ClassNotFoundException {

        Connection connection = ConnectionProvider.createConnection();
        String addressQuarry = "UPDATE person SET person.first_name = '" + firstName + "',person.last_name = '" + lastName +  "' WHERE person.id='" + id + "';";
        PreparedStatement preparedStatement = connection.prepareStatement(addressQuarry);
        preparedStatement.execute();
    }

    public static List<Person> getAllPerson() throws SQLException, ClassNotFoundException {
        String addressQuarry = "SELECT * FROM person;";
        return getPersonList(addressQuarry);
    }
    public static Person getPersonById(int id) throws SQLException, ClassNotFoundException {

        String quarry = "SELECT  * FROM person WHERE id='"+id+"';";
        return getPerson(quarry);
    }

    public static Person getPersonByPhone(String phoneNo) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT  * FROM person WHERE phone_number='"+phoneNo+"';";
        return getPerson(quarry);

    }
    public static  List<Person> getPersonWhoReadyToDonate() throws SQLException, ClassNotFoundException {
        String quarry = "SELECT  * FROM person WHERE ready_to_donate = 1;";
        return getPersonList(quarry);


    }
    public static ObservableList<ShowPerson> getPersonWhoReadyToDonate2() throws SQLException, ClassNotFoundException {
        String quarry = "SELECT id, first_name, last_name, email, blood_group, phone_number FROM person WHERE " +
                "ready_to_donate = 1;";
        return getPersonObservableList(quarry);


    }
    public static ObservableList<ShowPerson> getPersonWhoNeedBlood2() throws SQLException, ClassNotFoundException {
        String quarry = "SELECT id, first_name, last_name, email, blood_group, phone_number FROM person WHERE " +
                "need_blood = 1;";
        return getPersonObservableList(quarry);


    }

    private static ObservableList<ShowPerson> getPersonObservableList(String quarry) throws ClassNotFoundException, SQLException {
        int id;
        String name;
        String phoneNumber;
        String email;
        String bloodGroup;
        ObservableList<ShowPerson> list = FXCollections.observableArrayList();
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            id=resultSet.getInt("id");
            bloodGroup= resultSet.getString("blood_group");
            email = resultSet.getString("email");
            name=resultSet.getString("first_name") +" "+ resultSet.getString("last_name");
            phoneNumber=resultSet.getString("phone_number");
            if (id==User.getInstance().getUserId()) continue;
            list.add(new ShowPerson(id,name,email,phoneNumber,bloodGroup));

        }
        return list;
    }

    public static  List<Person> getPersonWhoNeedBlood() throws SQLException, ClassNotFoundException {
        String quarry = "SELECT  * FROM person WHERE need_blood = 1;";
        return getPersonList(quarry);

    }
    public static Person getPersonWithGivenDonnerID(int id) throws SQLException, ClassNotFoundException {
        return getPersonById(id);

    }
    public static Person getPersonWithReceivedDonnerId(int id) throws SQLException, ClassNotFoundException {


        return getPersonById(id);
    }

    private static Person getPerson(String quarry) throws SQLException, ClassNotFoundException {
        int addressId;
        int passwordId;
        int id;
        String firstName;
        String lastName;
        String phoneNumber;
        String email;
        String dateOfBirth;
        String bloodGroup;
        String gender;
        boolean readyToDonate ;
        boolean needBlood;
        Person person = null;
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            id=resultSet.getInt("id");
            bloodGroup= resultSet.getString("blood_group");
            dateOfBirth=resultSet.getString("date_of_birth");
            email = resultSet.getString("email");
            firstName=resultSet.getString("first_name");
            lastName=resultSet.getString("last_name");
            gender=resultSet.getString("gender");
            phoneNumber=resultSet.getString("phone_number");
            readyToDonate=resultSet.getBoolean("ready_to_donate");
            needBlood=resultSet.getBoolean("need_blood");
            passwordId=resultSet.getInt("password_id");
            addressId=resultSet.getInt("address_id");
            person= new  Person(addressId,passwordId,id,firstName,lastName,phoneNumber,email,dateOfBirth,bloodGroup,gender,readyToDonate,needBlood);


        }
        return person ;

    }
    private static  List<Person> getPersonList(String quarry) throws SQLException, ClassNotFoundException {
        int addressId;
        int passwordId;
        int id;
        String firstName;
        String lastName;
        String phoneNumber;
        String email;
        String dateOfBirth;
        String bloodGroup;
        String gender;
        boolean readyToDonate ;
        boolean needBlood;
        List<Person> personList = new ArrayList<>();
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            id=resultSet.getInt("id");
            bloodGroup= resultSet.getString("blood_group");
            dateOfBirth=resultSet.getString("date_of_birth");
            email = resultSet.getString("email");
            firstName=resultSet.getString("first_name");
            lastName=resultSet.getString("last_name");
            gender=resultSet.getString("gender");
            phoneNumber=resultSet.getString("phone_number");
            readyToDonate=resultSet.getBoolean("ready_to_donate");
            needBlood=resultSet.getBoolean("need_blood");
            passwordId=resultSet.getInt("password_id");
            addressId=resultSet.getInt("address_id");
            personList.add(new Person(addressId,passwordId,id,firstName,lastName,phoneNumber,email,dateOfBirth,bloodGroup,gender,readyToDonate,needBlood));


        }
        return personList;

    }

    public static boolean readyToDonateEvent(int personId) throws SQLException, ClassNotFoundException {
        String eventName= User.getInstance().getName().split("")[0];
        String quarry="CREATE EVENT "+eventName+" ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 2 MINUTE DO UPDATE person SET ready_to_donate = 1 WHERE id="+personId+";";
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        return preparedStatement.execute();
    }
    public static boolean setNeedBloodTrue(int id) throws SQLException, ClassNotFoundException {
        String quarry="UPDATE person SET need_blood = 1 WHERE id="+id+";";
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        return preparedStatement.execute();
    }
    public static boolean setReadyToDonate(int id, boolean status) throws SQLException, ClassNotFoundException {
        String quarry="UPDATE person SET ready_to_donate = " + (status ? 1 : 0) + " WHERE id="+id+";";
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        return preparedStatement.execute();
    }
    public static boolean setReadyToDonateFalse(int id) throws SQLException, ClassNotFoundException {
        return setReadyToDonate(id, false);
    }
    public static boolean setNeedBlood(int id, boolean status) throws SQLException, ClassNotFoundException {
        String quarry="UPDATE person SET need_blood = " + (status ? 1 : 0) + " WHERE id="+id+";";
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        return preparedStatement.execute();
    }
    public static boolean getDonorAvailabilityStatus(int id) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT ready_to_donate FROM person WHERE id="+id+";";
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getBoolean("ready_to_donate");
        }
        return false;
    }
    public static boolean getReadyToDonate(int id) throws SQLException, ClassNotFoundException {
        return getDonorAvailabilityStatus(id);
    }
    public static ObservableList<ShowPerson> getAvailableDonorsByBloodGroup(String bloodGroup) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT id, first_name, last_name, email, blood_group, phone_number FROM person WHERE " +
                "ready_to_donate = 1 AND blood_group = '" + bloodGroup + "';";
        return getPersonObservableList(quarry);
    }
    public static ObservableList<ShowPerson> getAvailableDonorsByLocation(String division, String district) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT p.id, p.first_name, p.last_name, p.email, p.blood_group, p.phone_number " +
                "FROM person p JOIN address a ON p.address_id = a.id WHERE " +
                "p.ready_to_donate = 1 AND a.division = '" + division + "' AND a.district = '" + district + "';";
        return getPersonObservableList(quarry);
    }
    public static boolean getNeedBloodStatus(int id) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT need_blood FROM person WHERE id="+id+";";
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getBoolean("need_blood");
        }
        return false;
    }
    public static ObservableList<ShowPerson> getRecipientsByBloodGroup(String bloodGroup) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT id, first_name, last_name, email, blood_group, phone_number FROM person WHERE " +
                "need_blood = 1 AND blood_group = '" + bloodGroup + "';";
        return getPersonObservableList(quarry);
    }
    public static ObservableList<ShowPerson> getRecipientsByLocation(String division, String district) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT p.id, p.first_name, p.last_name, p.email, p.blood_group, p.phone_number " +
                "FROM person p JOIN address a ON p.address_id = a.id WHERE " +
                "p.need_blood = 1 AND a.division = '" + division + "' AND a.district = '" + district + "';";
        return getPersonObservableList(quarry);
    }
    public static String getGenderById(int id) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT  gender FROM person WHERE id='" + id + "';";
        String gender = "";
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            gender = resultSet.getString("gender");
        }
        return gender;

    }
    public static int getAddressId(int ida) throws SQLException, ClassNotFoundException {
        String quarry = "SELECT  address_id FROM person WHERE id='" + ida + "';";
        int id = -1;
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(quarry);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            id = resultSet.getInt("address_id");
        }
        return id;
    }

    // Method to get email by phone number for password reset functionality
    public static String getEmailByPhoneNo(String phoneNumber) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionProvider.createConnection();
            String query = "SELECT email FROM person WHERE phone_number = ? AND is_active = TRUE";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, phoneNumber);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("email");
            }

            return null; // Phone number not found

        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        }
    }

    static String usingRandomUUID() {

        UUID randomUUID = UUID.randomUUID();

        return randomUUID.toString().replaceAll("-", "").substring(0,6);

    }
}
