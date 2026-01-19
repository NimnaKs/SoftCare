package me.nimnakse.water_management.inventory.consumptions.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryConsumptionCreateReq(
        @NotNull(message = "Inventory template id is required") Long inventoryTemplateId,
        Long expenseAccountId,
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be positive") BigDecimal quantity,
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.00", message = "Total amount must be positive") BigDecimal totalAmount,
        @NotNull(message = "Consumption date is required") LocalDate consumedAt,
        @NotBlank(message = "Batch number is required")
        @Size(max = 50, message = "Batch number cannot exceed 50 characters") String batchNo,
        @Size(max = 100, message = "Reference number cannot exceed 100 characters") String referenceNo,
        @Size(max = 255, message = "Description cannot exceed 255 characters") String description
) {
}
