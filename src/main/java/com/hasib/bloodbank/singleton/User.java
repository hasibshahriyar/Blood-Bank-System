package com.hasib.bloodbank.singleton;


import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.utils.NetworkUtility;

public class User {
    private static final User instance = new User();
    private int userId;



    private String name;
    private String userEmail;
    private String userPhoneNo;
    private ShowPerson showPerson;

    public ShowPerson getShowPerson() {
        return showPerson;
    }

    public void setShowPerson(ShowPerson showPerson) {
        this.showPerson = showPerson;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        System.out.println(bloodGroup + "from set bloodgroup");
        this.bloodGroup = bloodGroup;
    }

    private String message;
    private NetworkUtility networkUtility;
    private String bloodGroup;

    public NetworkUtility getNetworkUtility() {
        return networkUtility;
    }

    public void setNetworkUtility(NetworkUtility networkUtility) {
        this.networkUtility = networkUtility;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private User() {
    }

    public static User getInstance() {
        return instance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoneNo() {
        return userPhoneNo;
    }

    public void setUserPhoneNo(String userPhoneNo) {
        this.userPhoneNo = userPhoneNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
