package me.nimnakse.water_management.subscription_payments.controller;

import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.subscription_payments.dto.response.SubscriptionPaymentRequestRes;
import me.nimnakse.water_management.subscription_payments.service.SubscriptionPaymentRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/subscription/payment-requests")
@CrossOrigin
public class SubscriptionPaymentRequestController {

    private final SubscriptionPaymentRequestService service;

    public SubscriptionPaymentRequestController(SubscriptionPaymentRequestService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPaymentRequestRes>>> list(@RequestParam Long agencyId) {
        return ResponseEntity.ok(ApiResponse.success(service.list(agencyId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPaymentRequestRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<byte[]> attachment(@PathVariable Long id) throws IOException {
        File file = service.loadAttachment(id);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        String probe = Files.probeContentType(file.toPath());
        if (probe != null && !probe.isBlank()) {
            contentType = MediaType.parseMediaType(probe);
        }
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(Files.readAllBytes(file.toPath()));
    }

    @PostMapping(value = "/preview", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<SubscriptionPaymentRequestRes>> createPreview(
            @RequestParam Long agencyId,
            @RequestParam Long paymentMethodId,
            @RequestParam String reference,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidDate,
            @RequestParam BigDecimal amount,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                service.createPreview(agencyId, paymentMethodId, reference, paidDate, amount, file)
        ));
    }

    @PostMapping("/{id}/proceed")
    public ResponseEntity<ApiResponse<SubscriptionPaymentRequestRes>> proceed(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.proceed(id)));
    }
}
