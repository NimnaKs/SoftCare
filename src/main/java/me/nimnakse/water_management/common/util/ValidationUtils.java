package me.nimnakse.water_management.common.util;

import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern SRI_LANKA_MOBILE = Pattern.compile(ValidationPatterns.SRI_LANKA_MOBILE_REGEX);
    private static final Pattern SRI_LANKA_PHONE = Pattern.compile(ValidationPatterns.SRI_LANKA_PHONE_REGEX);

    private ValidationUtils() {
    }

    public static boolean isValidSriLankaMobile(String mobileNumber) {
        if (mobileNumber == null) {
            return false;
        }
        return SRI_LANKA_MOBILE.matcher(mobileNumber).matches();
    }

    public static boolean isValidSriLankaPhone(String number) {
        if (number == null) {
            return false;
        }
        return SRI_LANKA_PHONE.matcher(number).matches();
    }
}
