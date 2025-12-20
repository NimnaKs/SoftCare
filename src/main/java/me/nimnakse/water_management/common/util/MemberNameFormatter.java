package me.nimnakse.water_management.common.util;

import java.util.ArrayList;
import java.util.List;

public final class MemberNameFormatter {
    private MemberNameFormatter() {
    }

    public static String formatPersonalDisplayName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        List<String> parts = normalizeTokens(fullName);
        if (parts.size() <= 2) {
            return String.join(" ", parts);
        }
        List<String> output = new ArrayList<>();
        for (int index = 0; index < parts.size() - 2; index += 1) {
            String token = parts.get(index);
            if (!token.isBlank()) {
                output.add(token.substring(0, 1).toUpperCase());
            }
        }
        output.add(parts.get(parts.size() - 2));
        output.add(parts.get(parts.size() - 1));
        return String.join(" ", output);
    }

    public static String formatCorporateDisplayName(String corporateName) {
        if (corporateName == null || corporateName.isBlank()) {
            return null;
        }
        return String.join(" ", normalizeTokens(corporateName));
    }

    private static List<String> normalizeTokens(String name) {
        String[] rawParts = name.trim().split("\\s+");
        List<String> parts = new ArrayList<>();
        for (String raw : rawParts) {
            if (raw.isBlank()) {
                continue;
            }
            if (raw.equals(raw.toUpperCase())) {
                parts.add(raw);
            } else {
                String lower = raw.toLowerCase();
                String formatted = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
                parts.add(formatted);
            }
        }
        return parts;
    }
}
