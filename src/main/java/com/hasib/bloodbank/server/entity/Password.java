package com.hasib.bloodbank.server.entity;


public class Password {

    private int id;
    private String password;

    public Password(String password) {
        this.password = password;
    }

    public Password() {
    }

    public int getId() {
        return id;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
