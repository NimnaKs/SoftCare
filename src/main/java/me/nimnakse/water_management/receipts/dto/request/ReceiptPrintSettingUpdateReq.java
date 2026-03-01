package me.nimnakse.water_management.receipts.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReceiptPrintSettingUpdateReq(@NotNull Boolean showSettlementsOnPrint) {}
