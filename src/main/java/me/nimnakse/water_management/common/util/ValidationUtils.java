package me.nimnakse.water_management.common.util;

import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern SRI_LANKA_MOBILE = Pattern.compile("^07\\d{8}$");
    private static final Pattern TEN_DIGIT_NUMBER = Pattern.compile("^\\d{10}$");

    private ValidationUtils() {
    }

    public static boolean isValidSriLankaMobile(String mobileNumber) {
        if (mobileNumber == null) {
            return false;
        }
        return SRI_LANKA_MOBILE.matcher(mobileNumber).matches();
    }

    public static boolean isTenDigitNumber(String number) {
        if (number == null) {
            return false;
        }
        return TEN_DIGIT_NUMBER.matcher(number).matches();
    }
}
