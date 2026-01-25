package me.nimnakse.water_management.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        String messageEn,
        String messageSn,
        ErrorCode code,
        List<String> details,
        OffsetDateTime timestamp) {
    public static ApiError of(String messageEn, String messageSn, ErrorCode code, List<String> details) {
        return new ApiError(messageEn, messageSn, code, details, OffsetDateTime.now());
    }
}
