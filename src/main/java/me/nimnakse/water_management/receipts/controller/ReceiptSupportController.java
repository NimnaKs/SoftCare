package me.nimnakse.water_management.receipts.controller;

import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.receipts.dto.request.ReceiptPrintSettingUpdateReq;
import me.nimnakse.water_management.receipts.dto.response.*;
import me.nimnakse.water_management.receipts.service.ReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class ReceiptSupportController {
    private final ReceiptService receiptService;

    public ReceiptSupportController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/connections/by-account-number/{accountNo}")
    public ResponseEntity<ApiResponse<ConnectionByAccountRes>> byAccount(@PathVariable String accountNo) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.findConnectionByAccountNumber(accountNo)));
    }

    @GetMapping("/cheques/{chequeNo}")
    public ResponseEntity<ApiResponse<ChequeTrackingRes>> cheque(@PathVariable String chequeNo) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.findCheque(chequeNo)));
    }

    @PostMapping("/bulk-receipts/batches")
    public ResponseEntity<ApiResponse<BulkReceiptUploadBatchRes>> uploadBatch(
            @RequestParam Long orgUnitId,
            @RequestParam Long cashAccountId,
            @RequestParam String paymentMethodCode,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.uploadBatch(orgUnitId, cashAccountId, paymentMethodCode, file)));
    }

    @PostMapping("/bulk-receipts/batches/{id}/process")
    public ResponseEntity<ApiResponse<BulkReceiptUploadBatchRes>> processBatch(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.processBatch(id)));
    }

    @GetMapping("/bulk-receipts/batches/{id}/rows")
    public ResponseEntity<ApiResponse<PageResponse<BulkReceiptUploadRowRes>>> rows(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.getBatchRows(id, page, size)));
    }

    @GetMapping("/bulk-receipts/batches/{id}/failure-report")
    public ResponseEntity<byte[]> failureReport(@PathVariable Long id) {
        byte[] csv = receiptService.getFailureReportCsv(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-batch-" + id + "-errors.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    @GetMapping("/settings/receipt-print")
    public ResponseEntity<ApiResponse<ReceiptPrintSettingRes>> getPrintSetting() {
        return ResponseEntity.ok(ApiResponse.success(receiptService.getPrintSetting()));
    }

    @PutMapping("/settings/receipt-print")
    public ResponseEntity<ApiResponse<ReceiptPrintSettingRes>> updatePrintSetting(@Valid @RequestBody ReceiptPrintSettingUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.updatePrintSetting(request)));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodRes>>> paymentMethods() {
        return ResponseEntity.ok(ApiResponse.success(receiptService.paymentMethods()));
    }
}
