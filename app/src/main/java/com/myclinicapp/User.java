package com.myclinicapp;

public class User {

    public String username;
    public String fName;
    public String lName;
    public String password;
    public String type;
    public String email;
    public String clinicID;

    public User() {

    }

    public User(String username, String fName, String lName, String password, String type, String email) {
        this.username = username;
        this.fName = fName;
        this.lName = lName;
        this.password = password;
        this.type = type;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", password='" + password + '\'' +
                ", type='" + type + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
