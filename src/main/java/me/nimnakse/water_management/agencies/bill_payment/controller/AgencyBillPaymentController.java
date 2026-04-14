package me.nimnakse.water_management.agencies.bill_payment.controller;

import java.util.List;
import me.nimnakse.water_management.agencies.bill_payment.dto.request.AgencyBillPaymentCreateReq;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBalanceRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyBillPaymentRes;
import me.nimnakse.water_management.agencies.bill_payment.dto.response.AgencyLedgerEntryRes;
import me.nimnakse.water_management.agencies.bill_payment.service.AgencyBillPaymentService;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupCashAccountRes;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agency/bill-payment")
@CrossOrigin
public class AgencyBillPaymentController {
    private final AgencyBillPaymentService service;

    public AgencyBillPaymentController(AgencyBillPaymentService service) {
        this.service = service;
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<AgencyBalanceRes>> balances() {
        return ResponseEntity.ok(ApiResponse.success(service.getBalances()));
    }

    @GetMapping("/cash-accounts")
    public ResponseEntity<ApiResponse<List<AgencyTopupCashAccountRes>>> cashAccounts() {
        return ResponseEntity.ok(ApiResponse.success(service.listCashAccounts()));
    }

    @GetMapping("/cash-accounts/{cashAccountId}/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodRes>>> paymentMethods(@PathVariable Long cashAccountId) {
        return ResponseEntity.ok(ApiResponse.success(service.listPaymentMethods(cashAccountId)));
    }

    @GetMapping("/ledger/transaction")
    public ResponseEntity<ApiResponse<List<AgencyLedgerEntryRes>>> transactionLedger() {
        return ResponseEntity.ok(ApiResponse.success(service.listTransactionLedger()));
    }

    @GetMapping("/ledger/subscription")
    public ResponseEntity<ApiResponse<List<AgencyLedgerEntryRes>>> subscriptionLedger() {
        return ResponseEntity.ok(ApiResponse.success(service.listSubscriptionLedger()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AgencyBillPaymentRes>> create(@RequestBody AgencyBillPaymentCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }
}
