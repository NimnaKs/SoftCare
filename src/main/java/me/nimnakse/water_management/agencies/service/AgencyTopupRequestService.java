package me.nimnakse.water_management.agencies.service;

import me.nimnakse.water_management.agencies.dto.response.AgencyTopupCashAccountRes;
import me.nimnakse.water_management.agencies.dto.response.AgencyTopupRequestRes;
import me.nimnakse.water_management.receipts.dto.response.PaymentMethodRes;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AgencyTopupRequestService {
    List<AgencyTopupCashAccountRes> listTopupCashAccounts();

    List<PaymentMethodRes> listPaymentMethods(Long cashAccountId);

    AgencyTopupRequestRes create(
            Long cashAccountId,
            Long paymentMethodId,
            String reference,
            LocalDate paidDate,
            BigDecimal amount,
            MultipartFile file
    );
}

