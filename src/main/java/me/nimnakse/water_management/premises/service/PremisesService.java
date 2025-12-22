package me.nimnakse.water_management.premises.service;

import java.util.List;
import me.nimnakse.water_management.premises.dto.request.PremisesCreateReq;
import me.nimnakse.water_management.premises.dto.request.PremisesUpdateReq;
import me.nimnakse.water_management.premises.dto.response.PremisesRes;

public interface PremisesService {
    PremisesRes create(PremisesCreateReq request);

    PremisesRes update(Long id, PremisesUpdateReq request);

    PremisesRes getById(Long id);

    List<PremisesRes> list(Long billingZoneId);

    void delete(Long id);
}
