package me.nimnakse.water_management.purchases.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftCreateReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftUpdateReq;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderDraftRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderRes;
import me.nimnakse.water_management.purchases.service.PurchaseOrderDraftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase-order-drafts")
@Tag(name = "Purchase Order Drafts", description = "Purchase order draft operations")
@CrossOrigin
public class PurchaseOrderDraftController {
    private final PurchaseOrderDraftService draftService;

    public PurchaseOrderDraftController(PurchaseOrderDraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    @Operation(summary = "Create purchase order draft")
    public ResponseEntity<ApiResponse<PurchaseOrderDraftRes>> create(@Valid @RequestBody PurchaseOrderDraftCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(draftService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase order draft unit costs")
    public ResponseEntity<ApiResponse<PurchaseOrderDraftRes>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody PurchaseOrderDraftUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(draftService.update(id, request)));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept purchase order draft")
    public ResponseEntity<ApiResponse<PurchaseOrderDraftRes>> accept(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.accept(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel purchase order draft")
    public ResponseEntity<ApiResponse<PurchaseOrderDraftRes>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.cancel(id)));
    }

    @PostMapping("/{id}/convert-to-po")
    @Operation(summary = "Convert draft to purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderRes>> convertToPurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.convertToPurchaseOrder(id)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order draft")
    public ResponseEntity<ApiResponse<PurchaseOrderDraftRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List purchase order drafts")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDraftRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getPage(page, size)));
    }
}
