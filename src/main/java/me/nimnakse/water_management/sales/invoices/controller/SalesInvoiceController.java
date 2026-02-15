package me.nimnakse.water_management.sales.invoices.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.sales.invoices.dto.SalesInvoicePostAction;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceConnectionPreviewReq;
import me.nimnakse.water_management.sales.invoices.dto.request.SalesInvoiceCreateReq;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceConnectionPreviewRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoicePostRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoicePrintableConnectionsRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceRes;
import me.nimnakse.water_management.sales.invoices.dto.response.SalesInvoiceSummaryRes;
import me.nimnakse.water_management.sales.invoices.entity.SaleType;
import me.nimnakse.water_management.sales.invoices.service.SalesInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/invoices")
@CrossOrigin
@Tag(name = "Sales Invoices", description = "Customer and non-customer sales invoices")
public class SalesInvoiceController {
    private final SalesInvoiceService salesInvoiceService;

    public SalesInvoiceController(SalesInvoiceService salesInvoiceService) {
        this.salesInvoiceService = salesInvoiceService;
    }

    @PostMapping("/preview-connections")
    @Operation(summary = "Preview included connections")
    public ResponseEntity<ApiResponse<SalesInvoiceConnectionPreviewRes>> previewConnections(
            @Valid @RequestBody SalesInvoiceConnectionPreviewReq request,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.previewConnections(request, sort)));
    }

    @PostMapping
    @Operation(summary = "Create draft sales invoice")
    public ResponseEntity<ApiResponse<SalesInvoiceRes>> createDraft(@Valid @RequestBody SalesInvoiceCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.createDraftInvoice(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales invoice by id")
    public ResponseEntity<ApiResponse<SalesInvoiceRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List sales invoices")
    public ResponseEntity<ApiResponse<PageResponse<SalesInvoiceSummaryRes>>> list(
            @RequestParam(required = false) SaleType saleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.listInvoices(saleType, page, size)));
    }

    @PostMapping("/{id}/post")
    @Operation(summary = "Post draft invoice")
    public ResponseEntity<ApiResponse<SalesInvoicePostRes>> postInvoice(
            @PathVariable Long id,
            @RequestParam SalesInvoicePostAction action) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.postInvoice(id, action)));
    }

    @PostMapping("/{id}/print-connections")
    @Operation(summary = "Return printable connection list payload")
    public ResponseEntity<ApiResponse<SalesInvoicePrintableConnectionsRes>> printConnections(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.printConnections(id, page, size)));
    }

    @PostMapping("/{id}/settle")
    @Operation(summary = "Settle a posted non-customer invoice")
    public ResponseEntity<ApiResponse<SalesInvoiceRes>> settle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.settleNonCustomerInvoice(id)));
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Reverse a non-customer invoice")
    public ResponseEntity<ApiResponse<SalesInvoiceRes>> reverse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salesInvoiceService.reverseNonCustomerInvoice(id)));
    }
}
