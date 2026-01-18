package me.nimnakse.water_management.purchases.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import me.nimnakse.water_management.purchases.service.GrnInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grn-invoices")
@Tag(name = "GRN Invoices", description = "Goods received note operations")
@CrossOrigin
public class GrnInvoiceController {
    private final GrnInvoiceService grnInvoiceService;

    public GrnInvoiceController(GrnInvoiceService grnInvoiceService) {
        this.grnInvoiceService = grnInvoiceService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get GRN invoice")
    public ResponseEntity<ApiResponse<GrnInvoiceRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(grnInvoiceService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List GRN invoices")
    public ResponseEntity<ApiResponse<PageResponse<GrnInvoiceRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(grnInvoiceService.getPage(page, size)));
    }
}
