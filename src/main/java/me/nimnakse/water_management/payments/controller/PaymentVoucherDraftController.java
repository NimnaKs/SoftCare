package me.nimnakse.water_management.payments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftCreateReq;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftUpdateReq;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherDraftRes;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherRes;
import me.nimnakse.water_management.payments.service.PaymentVoucherDraftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment-voucher-drafts")
@Tag(name = "Payment Voucher Drafts", description = "Payment voucher draft operations")
@CrossOrigin
public class PaymentVoucherDraftController {
    private final PaymentVoucherDraftService draftService;

    public PaymentVoucherDraftController(PaymentVoucherDraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    @Operation(summary = "Create payment voucher draft")
    public ResponseEntity<ApiResponse<PaymentVoucherDraftRes>> create(
            @Valid @RequestBody PaymentVoucherDraftCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(draftService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment voucher draft amounts")
    public ResponseEntity<ApiResponse<PaymentVoucherDraftRes>> update(@PathVariable Long id,
            @Valid @RequestBody PaymentVoucherDraftUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(draftService.update(id, request)));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept payment voucher draft")
    public ResponseEntity<ApiResponse<PaymentVoucherDraftRes>> accept(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.accept(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel payment voucher draft")
    public ResponseEntity<ApiResponse<PaymentVoucherDraftRes>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.cancel(id)));
    }

    @PostMapping("/{id}/convert-to-voucher")
    @Operation(summary = "Convert draft to payment voucher")
    public ResponseEntity<ApiResponse<PaymentVoucherRes>> convertToPaymentVoucher(
            @PathVariable Long id,
            @Valid @RequestBody me.nimnakse.water_management.payments.dto.request.PaymentVoucherConvertReq request) {
        return ResponseEntity.ok(ApiResponse.success(draftService.convertToPaymentVoucher(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment voucher draft")
    public ResponseEntity<ApiResponse<PaymentVoucherDraftRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List payment voucher drafts")
    public ResponseEntity<ApiResponse<PageResponse<PaymentVoucherDraftRes>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getPage(page, size)));
    }
}
