package me.nimnakse.water_management.valves.service;

import java.util.List;
import me.nimnakse.water_management.valves.dto.request.ValveCreateReq;
import me.nimnakse.water_management.valves.dto.request.ValveUpdateReq;
import me.nimnakse.water_management.valves.dto.response.ValveRes;

public interface ValveService {
    ValveRes create(ValveCreateReq request);

    ValveRes update(Long id, ValveUpdateReq request);

    ValveRes getById(Long id);

    List<ValveRes> list(Long orgUnitId);

    void delete(Long id);
}
