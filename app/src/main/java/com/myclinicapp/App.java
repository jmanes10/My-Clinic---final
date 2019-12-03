package com.myclinicapp;

import android.app.Application;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class App extends Application {

    static final String FIREBASE_DB_PATH_USERS = "users";
    static final String FIREBASE_DB_PATH_SERVICES = "services";
    static final String FIREBASE_DB_PATH_PATIENT_USERS = "patient_users";
    static final String FIREBASE_DB_PATH_CLINIC_USERS = "clinic_users";
    static final String FIREBASE_DB_PATH_CLINICS = "clinics";
    static final String FIREBASE_DB_PATH_TS = "ts";
    static User user;

    public static String getUserGreetings() {
        if (user == null) return "";
        return "Welcome " + user.fName + "! You are logged-in as a " + user.type;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static String getSHA224String(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-224");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUser() {
        return user;
    }

    public static void setUser(User user) {
        App.user = user;
    }
}
