package me.nimnakse.water_management.billing_zones.service;

import java.util.List;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneCreateReq;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneReorderReq;
import me.nimnakse.water_management.billing_zones.dto.request.BillingZoneUpdateReq;
import me.nimnakse.water_management.billing_zones.dto.response.BillingZoneRes;

public interface BillingZoneService {
    BillingZoneRes create(BillingZoneCreateReq request);

    BillingZoneRes update(Long id, BillingZoneUpdateReq request);

    BillingZoneRes getById(Long id);

    List<BillingZoneRes> list(Long orgUnitId);

    void delete(Long id);

    List<BillingZoneRes> reorder(BillingZoneReorderReq request);
}
