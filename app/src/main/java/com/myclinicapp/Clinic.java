package com.myclinicapp;

import java.util.List;

public class Clinic {

    public static final int PAYMENT_METHOD_DEFAULT = -1;
    public static final int PAYMENT_METHOD_CASH = 1;
    public static final int PAYMENT_METHOD_CREDIT_DEBIT = 2;
    public static final int PAYMENT_METHOD_BOTH = 0;

    public String name;
    public String address;
    public String phone;
    public List<String> insuranceTypes;
    public int paymentMethodAccepted = PAYMENT_METHOD_DEFAULT;
    public int openHour = -1;
    public int openMinute = -1;
    public int closeHour = -1;
    public int closeMinute = -1;
    public List<Boolean> workingDays;
    public List<String> servicesIdsList;
    public long waitingHours;

    public Clinic() {

    }

    public Clinic(String name, String address, String phone, List<String> insuranceTypes, int paymentMethodAccepted, int openHour, int openMinute, int closeHour, int closeMinute, List<Boolean> workingDays, List<String> servicesIdsList) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.insuranceTypes = insuranceTypes;
        this.paymentMethodAccepted = paymentMethodAccepted;
        this.openHour = openHour;
        this.openMinute = openMinute;
        this.closeHour = closeHour;
        this.closeMinute = closeMinute;
        this.workingDays = workingDays;
        this.servicesIdsList = servicesIdsList;
    }
}
