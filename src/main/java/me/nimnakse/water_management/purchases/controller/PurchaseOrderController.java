package me.nimnakse.water_management.purchases.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderAssignSupplierReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderConvertGrnReq;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderRes;
import me.nimnakse.water_management.purchases.service.PurchaseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase-orders")
@Tag(name = "Purchase Orders", description = "Purchase order operations")
@CrossOrigin
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List purchase orders")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.getPage(page, size)));
    }

    @PostMapping("/{id}/assign-supplier")
    @Operation(summary = "Assign supplier to purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderRes>> assignSupplier(@PathVariable Long id,
                                                                        @Valid @RequestBody PurchaseOrderAssignSupplierReq request) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.assignSupplier(id, request)));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderRes>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.reject(id)));
    }

    @PostMapping("/{id}/convert-to-grn")
    @Operation(summary = "Convert purchase order to GRN invoice")
    public ResponseEntity<ApiResponse<GrnInvoiceRes>> convertToGrn(@PathVariable Long id,
                                                                   @Valid @RequestBody PurchaseOrderConvertGrnReq request) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.convertToGrn(id, request)));
    }
}
