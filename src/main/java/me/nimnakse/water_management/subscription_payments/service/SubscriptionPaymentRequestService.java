package me.nimnakse.water_management.subscription_payments.service;

import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import me.nimnakse.water_management.subscription_payments.dto.response.SubscriptionPaymentRequestRes;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionPaymentRequestService {

    List<SubscriptionPaymentRequestRes> list(Long agencyId);

    List<PaymentMethodRes> listPaymentMethods();

    SubscriptionPaymentRequestRes createPreview(
            Long agencyId,
            Long paymentMethodId,
            String reference,
            LocalDate paidDate,
            BigDecimal amount,
            MultipartFile file
    );

    SubscriptionPaymentRequestRes proceed(Long id);
}