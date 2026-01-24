package me.nimnakse.water_management.purchases.vouchers.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.vouchers.dto.request.PurchaseVoucherConvertReq;
import me.nimnakse.water_management.purchases.vouchers.dto.request.PurchaseVoucherDraftCreateReq;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherDraftRes;
import me.nimnakse.water_management.purchases.vouchers.dto.response.PurchaseVoucherRes;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import java.util.List;

public interface PurchaseVoucherDraftService {
    PurchaseVoucherDraftRes create(PurchaseVoucherDraftCreateReq request);

    PurchaseVoucherRes convertToPurchaseVoucher(Long id, PurchaseVoucherConvertReq request);

    PurchaseVoucherDraftRes getById(Long id);

    PageResponse<PurchaseVoucherDraftRes> getPage(int page, int size);

    List<GrnInvoiceRes> getAvailableGrnInvoices(Long orgUnitId);
}
