package me.nimnakse.watermanagement.common.api;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        long totalItems,
        int totalPages,
        int page,
        int size
) {
}
