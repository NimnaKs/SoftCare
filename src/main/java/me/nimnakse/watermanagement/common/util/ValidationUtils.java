package me.nimnakse.watermanagement.common.util;

import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern SRI_LANKA_MOBILE = Pattern.compile("^07\\d{8}$");

    private ValidationUtils() {
    }

    public static boolean isValidSriLankaMobile(String mobileNumber) {
        if (mobileNumber == null) {
            return false;
        }
        return SRI_LANKA_MOBILE.matcher(mobileNumber).matches();
    }
}
