package me.nimnakse.water_management.clusters.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ClusterCreateReq(
                @NotBlank String name,
                Long orgUnitId) {
}
