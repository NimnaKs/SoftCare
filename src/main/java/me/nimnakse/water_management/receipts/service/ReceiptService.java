package me.nimnakse.water_management.receipts.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.receipts.dto.request.*;
import me.nimnakse.water_management.receipts.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {
    PageResponse<ReceiptListRes> list(int page, int size, String dateFrom, String dateTo, Long connectionId, String receiptNo, String status, String paymentMethod);
    ReceiptVerifyPasswordRes verifyPassword(ReceiptVerifyPasswordReq request);
    ConnectionByAccountRes findConnectionByAccountNumber(String accountNo);
    ReceiptSettlementPreviewRes previewSettlement(ReceiptSettlementPreviewReq request);
    ReceiptCreateRes create(ReceiptCreateReq request);
    void reverse(Long receiptId, ReceiptReverseReq request);
    ChequeTrackingRes findCheque(String chequeNo);
    BulkReceiptUploadBatchRes uploadBatch(Long orgUnitId, Long cashAccountId, String paymentMethodCode, MultipartFile file);
    BulkReceiptUploadBatchRes processBatch(Long batchId);
    PageResponse<BulkReceiptUploadRowRes> getBatchRows(Long batchId, int page, int size);
    byte[] getFailureReportCsv(Long batchId);
    ReceiptPrintSettingRes getPrintSetting();
    ReceiptPrintSettingRes updatePrintSetting(ReceiptPrintSettingUpdateReq request);
    void forwardUnrecognized(Long receiptId, UnrecognizedForwardReq request);
    java.util.List<PaymentMethodRes> paymentMethods();
}
