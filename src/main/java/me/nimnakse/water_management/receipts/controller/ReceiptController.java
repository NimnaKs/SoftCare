package me.nimnakse.water_management.receipts.controller;

import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.receipts.dto.request.ReceiptCreateReq;
import me.nimnakse.water_management.receipts.dto.request.ReceiptReverseReq;
import me.nimnakse.water_management.receipts.dto.request.ReceiptSettlementPreviewReq;
import me.nimnakse.water_management.receipts.dto.request.ReceiptVerifyPasswordReq;
import me.nimnakse.water_management.receipts.dto.request.UnrecognizedForwardReq;
import me.nimnakse.water_management.receipts.dto.response.ReceiptCreateRes;
import me.nimnakse.water_management.receipts.dto.response.ReceiptListRes;
import me.nimnakse.water_management.receipts.dto.response.ReceiptSettlementPreviewRes;
import me.nimnakse.water_management.receipts.dto.response.ReceiptVerifyPasswordRes;
import me.nimnakse.water_management.receipts.service.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/receipts")
@CrossOrigin
public class ReceiptController {
    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReceiptListRes>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long connectionId,
            @RequestParam(required = false) String receiptNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod
    ) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.list(page, size, dateFrom, dateTo, connectionId, receiptNo, status, paymentMethod)));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<ApiResponse<ReceiptVerifyPasswordRes>> verifyPassword(@Valid @RequestBody ReceiptVerifyPasswordReq request) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.verifyPassword(request)));
    }

    @PostMapping("/preview-settlement")
    public ResponseEntity<ApiResponse<ReceiptSettlementPreviewRes>> previewSettlement(@Valid @RequestBody ReceiptSettlementPreviewReq request) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.previewSettlement(request)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReceiptCreateRes>> create(@Valid @RequestBody ReceiptCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.create(request)));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<ApiResponse<Void>> reverse(@PathVariable Long id, @Valid @RequestBody ReceiptReverseReq request) {
        receiptService.reverse(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/unrecognized/forward")
    public ResponseEntity<ApiResponse<Void>> forwardUnrecognized(@PathVariable Long id, @RequestBody UnrecognizedForwardReq request) {
        receiptService.forwardUnrecognized(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
