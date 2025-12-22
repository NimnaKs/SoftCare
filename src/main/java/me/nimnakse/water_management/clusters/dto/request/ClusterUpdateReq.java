package me.nimnakse.water_management.clusters.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ClusterUpdateReq(
        @NotBlank String name
) {
}
