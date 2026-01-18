package me.nimnakse.water_management.suppliers.service;

import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.suppliers.dto.request.SupplierCreateReq;
import me.nimnakse.water_management.suppliers.dto.request.SupplierUpdateReq;
import me.nimnakse.water_management.suppliers.dto.response.SupplierRes;

public interface SupplierService {
    SupplierRes create(SupplierCreateReq request);

    SupplierRes update(Long id, SupplierUpdateReq request);

    SupplierRes getById(Long id);

    PageResponse<SupplierRes> getPage(int page, int size);

    void deactivate(Long id);
}
