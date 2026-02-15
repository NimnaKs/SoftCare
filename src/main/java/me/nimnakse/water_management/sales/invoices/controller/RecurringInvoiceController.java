package me.nimnakse.water_management.sales.invoices.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.sales.invoices.dto.request.RecurringInvoiceToggleReq;
import me.nimnakse.water_management.sales.invoices.dto.response.RecurringInvoiceRes;
import me.nimnakse.water_management.sales.invoices.service.SalesInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/recurring-invoices")
@CrossOrigin
@Tag(name = "Recurring Sales Invoices", description = "Recurring sales invoice operations")
public class RecurringInvoiceController {
    private final SalesInvoiceService salesInvoiceService;

    public RecurringInvoiceController(SalesInvoiceService salesInvoiceService) {
        this.salesInvoiceService = salesInvoiceService;
    }

    @GetMapping
    @Operation(summary = "List recurring invoices")
    public ResponseEntity<ApiResponse<PageResponse<RecurringInvoiceRes>>> list(
            @RequestParam(required = false) Long billingZoneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                salesInvoiceService.listRecurringInvoices(billingZoneId, page, size)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Enable/disable recurring invoice")
    public ResponseEntity<ApiResponse<RecurringInvoiceRes>> toggle(
            @PathVariable Long id,
            @Valid @RequestBody RecurringInvoiceToggleReq request) {
        return ResponseEntity.ok(ApiResponse.success(
                salesInvoiceService.toggleRecurring(id, request)));
    }
}
