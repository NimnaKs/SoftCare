package me.nimnakse.water_management.payments.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftCreateReq;
import me.nimnakse.water_management.payments.dto.request.PaymentVoucherDraftUpdateReq;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherDraftRes;
import me.nimnakse.water_management.payments.dto.response.PaymentVoucherRes;

public interface PaymentVoucherDraftService {
    PaymentVoucherDraftRes create(PaymentVoucherDraftCreateReq request);

    PaymentVoucherDraftRes update(Long id, PaymentVoucherDraftUpdateReq request);

    PaymentVoucherDraftRes accept(Long id);

    PaymentVoucherDraftRes cancel(Long id);

    PaymentVoucherRes convertToPaymentVoucher(Long id,
            me.nimnakse.water_management.payments.dto.request.PaymentVoucherConvertReq request);

    PaymentVoucherDraftRes getById(Long id);

    PageResponse<PaymentVoucherDraftRes> getPage(int page, int size);
}
