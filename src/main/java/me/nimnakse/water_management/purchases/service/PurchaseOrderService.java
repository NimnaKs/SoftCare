package me.nimnakse.water_management.purchases.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderAssignSupplierReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderConvertGrnReq;
import me.nimnakse.water_management.purchases.dto.response.GrnInvoiceRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderRes;

public interface PurchaseOrderService {
    PurchaseOrderRes getById(Long id);

    PageResponse<PurchaseOrderRes> getPage(int page, int size);

    PurchaseOrderRes assignSupplier(Long id, PurchaseOrderAssignSupplierReq request);

    PurchaseOrderRes reject(Long id);

    GrnInvoiceRes convertToGrn(Long id, PurchaseOrderConvertGrnReq request);
}
