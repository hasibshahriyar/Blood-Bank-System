package com.hasib.bloodbank.server.entity;


public class Address {
    private final String country = "Bangladesh";

    private int id;
    private String division;
    private String district;

    private String subDistrict;

    private Person person;

    public Address() {
    }

    public Address(String division, String district, String subDistrict) {
        this.division = division;
        this.district = district;
        this.subDistrict = subDistrict;
    }

    public Address(String division, String district, String subDistrict, Person person) {
        this.division = division;
        this.district = district;
        this.subDistrict = subDistrict;
        this.person = person;
    }

    public String getCountry() {
        return country;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getSubDistrict() {
        return subDistrict;
    }

    public void setSubDistrict(String subDistrict) {
        this.subDistrict = subDistrict;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", country='" + country + '\'' +
                ", division='" + division + '\'' +
                ", district='" + district + '\'' +
                ", subDistrict='" + subDistrict + '\'' +
                ", person=" + person +
                '}';
    }
}
