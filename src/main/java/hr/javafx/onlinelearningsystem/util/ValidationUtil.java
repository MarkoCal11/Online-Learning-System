package hr.javafx.onlinelearningsystem.util;

import hr.javafx.onlinelearningsystem.exception.UserDataFormatException;

public class ValidationUtil {
    private ValidationUtil() {}


    public static void validateEmail(String email) {
        if(!email.matches("^[A-Za-z0-9._%+-]+@ols\\.edu$")) {
            throw new UserDataFormatException("Invalid email address. Must end with @ols.edu");
        }
    }

    public static void validatePhone(String phone) {
        if(!phone.matches("^\\+385\\d{9}$")) {
            throw new UserDataFormatException("Invalid phone number. Correct format: +385XXXXXXXXX");
        }
    }

    public static void validateJmbag(String jmbag) {
        if(!jmbag.matches("^\\d{10}$")) {
            throw new UserDataFormatException("Invalid JMBAG. Must be 10 digits");
        }
    }
}
