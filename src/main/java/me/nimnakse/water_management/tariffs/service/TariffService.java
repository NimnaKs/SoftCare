package me.nimnakse.water_management.tariffs.service;

import java.util.List;
import me.nimnakse.water_management.tariffs.dto.request.TariffCreateReq;
import me.nimnakse.water_management.tariffs.dto.request.TariffUpdateReq;
import me.nimnakse.water_management.tariffs.dto.response.TariffRes;

public interface TariffService {
    TariffRes create(TariffCreateReq request);

    TariffRes update(Long id, TariffUpdateReq request);

    TariffRes getById(Long id);

    List<TariffRes> list(Long orgUnitId);

    void delete(Long id);
}
