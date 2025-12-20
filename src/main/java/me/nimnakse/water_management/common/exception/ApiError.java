package me.nimnakse.water_management.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        String message,
        ErrorCode code,
        List<String> details,
        OffsetDateTime timestamp
) {
    public static ApiError of(String message, ErrorCode code, List<String> details) {
        return new ApiError(message, code, details, OffsetDateTime.now());
    }
}
