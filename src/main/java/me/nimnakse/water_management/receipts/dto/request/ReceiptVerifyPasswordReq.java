package me.nimnakse.water_management.receipts.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReceiptVerifyPasswordReq(@NotBlank String password) {}
