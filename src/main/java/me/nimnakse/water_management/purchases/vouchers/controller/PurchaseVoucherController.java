package me.nimnakse.water_management.purchases.vouchers.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherRes;
import me.nimnakse.water_management.purchases.vouchers.service.PurchaseVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase-vouchers")
@Tag(name = "Purchase Vouchers", description = "Finalized purchase voucher operations")
@CrossOrigin
public class PurchaseVoucherController {

    private final PurchaseVoucherService voucherService;

    public PurchaseVoucherController(PurchaseVoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase voucher")
    public ResponseEntity<ApiResponse<PurchaseVoucherRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List purchase vouchers")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseVoucherRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getPage(page, size)));
    }
}
