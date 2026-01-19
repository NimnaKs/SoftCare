package me.nimnakse.water_management.payments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherRes;
import me.nimnakse.water_management.payments.service.PaymentVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment-vouchers")
@Tag(name = "Payment Vouchers", description = "Payment voucher operations")
@CrossOrigin
public class PaymentVoucherController {
    private final PaymentVoucherService voucherService;

    public PaymentVoucherController(PaymentVoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment voucher")
    public ResponseEntity<ApiResponse<PaymentVoucherRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List payment vouchers")
    public ResponseEntity<ApiResponse<PageResponse<PaymentVoucherRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getPage(page, size)));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject payment voucher")
    public ResponseEntity<ApiResponse<PaymentVoucherRes>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.reject(id)));
    }
}
