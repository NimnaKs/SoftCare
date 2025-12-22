package me.nimnakse.water_management.common.util;

public final class ValidationPatterns {
    public static final String SRI_LANKA_MOBILE_REGEX = "^07\\d{8}$";
    public static final String SRI_LANKA_PHONE_REGEX = "^0\\d{9}$";
    public static final String OPTIONAL_SRI_LANKA_PHONE_REGEX = "^$|0\\d{9}$";

    private ValidationPatterns() {
    }
}
