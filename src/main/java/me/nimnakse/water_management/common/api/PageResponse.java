package me.nimnakse.water_management.common.api;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        long totalItems,
        int totalPages,
        int page,
        int size
) {
}
