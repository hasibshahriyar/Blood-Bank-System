package com.hasib.bloodbank.server.entity;


import java.sql.Date;

public class Donation {

    private int id;
    private String hospitalName;
    private Date donationDate;
    private int donatedPersonId;
    private int receivedPersonId;
    private BloodGroup bloodGroup;

    public Donation(String hospitalName, Date donationDate, int donatedPersonId, int receivedPersonId, BloodGroup bloodGroup) {
        this.hospitalName = hospitalName;
        this.donationDate = donationDate;
        this.donatedPersonId = donatedPersonId;
        this.receivedPersonId = receivedPersonId;
        this.bloodGroup = bloodGroup;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }


    public int getDonatedPersonId() {
        return donatedPersonId;
    }

    public void setDonatedPersonId(int donatedPersonId) {
        this.donatedPersonId = donatedPersonId;
    }

    public int getReceivedPersonId() {
        return receivedPersonId;
    }

    public void setReceivedPersonId(int receivedPersonId) {
        this.receivedPersonId = receivedPersonId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public Date getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(Date donationDate) {
        this.donationDate = donationDate;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Donation{" +
                "id=" + id +
                ", hospitalName='" + hospitalName + '\'' +
                ", donationDate=" + donationDate +
                ", donatedPersonId=" + donatedPersonId +
                ", receivedPersonId=" + receivedPersonId +
                ", bloodGroup=" + bloodGroup +
                '}';
    }
}
