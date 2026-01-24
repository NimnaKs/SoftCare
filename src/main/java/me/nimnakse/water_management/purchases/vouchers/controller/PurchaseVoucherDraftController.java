package me.nimnakse.water_management.purchases.vouchers.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.vouchers.dto.request.PurchaseVoucherConvertReq;
import me.nimnakse.water_management.purchases.vouchers.dto.request.PurchaseVoucherDraftCreateReq;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherDraftRes;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherRes;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import me.nimnakse.water_management.purchases.vouchers.service.PurchaseVoucherDraftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/purchase-voucher-drafts")
@Tag(name = "Purchase Voucher Drafts", description = "Purchase voucher draft operations")
@CrossOrigin
public class PurchaseVoucherDraftController {
    private final PurchaseVoucherDraftService draftService;

    public PurchaseVoucherDraftController(PurchaseVoucherDraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    @Operation(summary = "Create purchase voucher draft")
    public ResponseEntity<ApiResponse<PurchaseVoucherDraftRes>> create(
            @Valid @RequestBody PurchaseVoucherDraftCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(draftService.create(request)));
    }

    @PostMapping("/{id}/convert-to-voucher")
    @Operation(summary = "Convert draft to purchase voucher")
    public ResponseEntity<ApiResponse<PurchaseVoucherRes>> convertToPurchaseVoucher(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseVoucherConvertReq request) {
        return ResponseEntity.ok(ApiResponse.success(draftService.convertToPurchaseVoucher(id, request)));
    }

    @GetMapping("/available-grns")
    @Operation(summary = "Get available GRN invoices for payment")
    public ResponseEntity<ApiResponse<List<GrnInvoiceRes>>> getAvailableGrnInvoices(@RequestParam Long orgUnitId) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getAvailableGrnInvoices(orgUnitId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase voucher draft")
    public ResponseEntity<ApiResponse<PurchaseVoucherDraftRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List purchase voucher drafts")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseVoucherDraftRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getPage(page, size)));
    }
}
