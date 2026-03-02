package me.nimnakse.water_management.subscription_payments.controller;

import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import me.nimnakse.water_management.subscription_payments.service.SubscriptionPaymentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscription/payment-requests")
@CrossOrigin
public class SubscriptionPaymentMethodController {
    private final SubscriptionPaymentRequestService service;

    public SubscriptionPaymentMethodController(SubscriptionPaymentRequestService service) {
        this.service = service;
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodRes>>> paymentMethods() {
        return ResponseEntity.ok(ApiResponse.success(service.listPaymentMethods()));
    }
}
