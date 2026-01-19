package me.nimnakse.water_management.payments.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherRes;

public interface PaymentVoucherService {
    PaymentVoucherRes getById(Long id);

    PageResponse<PaymentVoucherRes> getPage(int page, int size);

    PaymentVoucherRes reject(Long id);
}
