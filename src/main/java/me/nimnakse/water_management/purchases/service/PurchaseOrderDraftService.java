package me.nimnakse.water_management.purchases.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftCreateReq;
import me.nimnakse.water_management.purchases.dto.request.PurchaseOrderDraftUpdateReq;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderDraftRes;
import me.nimnakse.water_management.purchases.dto.response.PurchaseOrderRes;

public interface PurchaseOrderDraftService {
    PurchaseOrderDraftRes create(PurchaseOrderDraftCreateReq request);

    PurchaseOrderDraftRes update(Long id, PurchaseOrderDraftUpdateReq request);

    PurchaseOrderDraftRes accept(Long id);

    PurchaseOrderDraftRes cancel(Long id);

    PurchaseOrderRes convertToPurchaseOrder(Long id);

    PurchaseOrderDraftRes getById(Long id);

    PageResponse<PurchaseOrderDraftRes> getPage(int page, int size);
}
