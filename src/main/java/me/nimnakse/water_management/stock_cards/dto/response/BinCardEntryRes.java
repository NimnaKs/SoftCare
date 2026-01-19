package me.nimnakse.water_management.stock_cards.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BinCardEntryRes(
        LocalDate movementDate,
        String movement,
        String referenceNo,
        String batchNo,
        String description,
        BigDecimal qtyIn,
        BigDecimal qtyOut,
        BigDecimal balance
) {
}
