package me.nimnakse.water_management.service_requests.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ServiceRequestMobileNumberReq(@NotBlank String contactMobileNumber) {}
