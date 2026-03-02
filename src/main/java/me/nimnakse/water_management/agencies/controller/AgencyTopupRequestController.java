package me.nimnakse.water_management.agencies.controller;

import me.nimnakse.water_management.agencies.dto.response.AgencyTopupCashAccountRes;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupRequestRes;
import me.nimnakse.water_management.agencies.service.AgencyTopupRequestService;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/agency/topup-request")
@CrossOrigin
public class AgencyTopupRequestController {
    private final AgencyTopupRequestService agencyTopupRequestService;

    public AgencyTopupRequestController(AgencyTopupRequestService agencyTopupRequestService) {
        this.agencyTopupRequestService = agencyTopupRequestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AgencyTopupRequestRes>>> list() {
        return ResponseEntity.ok(ApiResponse.success(agencyTopupRequestService.list()));
    }

    @GetMapping("/cash-accounts")
    public ResponseEntity<ApiResponse<List<AgencyTopupCashAccountRes>>> cashAccounts() {
        return ResponseEntity.ok(ApiResponse.success(agencyTopupRequestService.listTopupCashAccounts()));
    }

    @GetMapping("/cash-accounts/{cashAccountId}/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodRes>>> paymentMethods(@PathVariable Long cashAccountId) {
        return ResponseEntity.ok(ApiResponse.success(agencyTopupRequestService.listPaymentMethods(cashAccountId)));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AgencyTopupRequestRes>> create(
            @RequestParam Long cashAccountId,
            @RequestParam Long paymentMethodId,
            @RequestParam String reference,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidDate,
            @RequestParam BigDecimal amount,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                agencyTopupRequestService.create(cashAccountId, paymentMethodId, reference, paidDate, amount, file)
        ));
    }
}
