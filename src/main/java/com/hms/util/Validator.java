package com.hms.util;

public class Validator {
    private Validator() {}

    public static boolean isEmpty(String text) {
        return text == null || text.isBlank();
    }

    public static boolean isValidPhone(String phone) {
        if(isEmpty(phone)) return false;
        return phone.matches("[+]?[0-9]{10,13}");
    }

    public static boolean isValidEmail(String email) {
        if(isEmpty(email)) return false;
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidName(String name){
        if(isEmpty(name)) return false;
        return name.matches("[a-zA-Z '-]{2,50}");
    }

    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    public static boolean isNonNegativeAmount(int value) {
        return value >= 0;
    }
}
