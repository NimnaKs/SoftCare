package me.nimnakse.water_management.sales.invoices.dto.request;

import jakarta.validation.constraints.NotNull;

public record RecurringInvoiceToggleReq(
        @NotNull(message = "enabled is required") Boolean enabled
) {
}
