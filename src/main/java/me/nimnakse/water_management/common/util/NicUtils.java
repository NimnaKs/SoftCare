package me.nimnakse.water_management.common.util;

import java.util.Optional;
import java.util.regex.Pattern;

public final class NicUtils {
    private static final Pattern OLD_NIC_PATTERN = Pattern.compile("^\\d{9}[VvXx]$");
    private static final Pattern NEW_NIC_PATTERN = Pattern.compile("^\\d{12}$");

    private NicUtils() {
    }

    public static Optional<NicParseResult> parse(String nic) {
        if (nic == null || nic.isBlank()) {
            return Optional.empty();
        }
        String trimmed = nic.trim();
        if (OLD_NIC_PATTERN.matcher(trimmed).matches()) {
            String numeric = trimmed.substring(0, 9);
            String newNic = toNewNic(numeric);
            return Optional.of(new NicParseResult(trimmed.toUpperCase(), newNic, numeric));
        }
        if (NEW_NIC_PATTERN.matcher(trimmed).matches()) {
            String oldNumeric = toOldNumeric(trimmed);
            return Optional.of(new NicParseResult(oldNumeric, trimmed, oldNumeric));
        }
        return Optional.empty();
    }

    public static String toNewNic(String oldNumeric) {
        String prefix = oldNumeric.substring(0, 5);
        String suffix = oldNumeric.substring(5);
        return "19" + prefix + "0" + suffix;
    }

    public static String toOldNumeric(String newNic) {
        String withoutCentury = newNic.substring(2);
        int insertionIndex = withoutCentury.length() - 4;
        return withoutCentury.substring(0, insertionIndex) + withoutCentury.substring(insertionIndex + 1);
    }

    public record NicParseResult(String oldNic, String newNic, String numericKey) {
    }
}
