package me.nimnakse.water_management.organization.service;

import java.util.List;
import me.nimnakse.water_management.organization.dto.request.WaterProjectCreateReq;
import me.nimnakse.water_management.organization.dto.request.WaterProjectUpdateReq;
import me.nimnakse.water_management.organization.dto.response.WaterProjectRes;

public interface WaterProjectService {
    WaterProjectRes create(WaterProjectCreateReq request);

    WaterProjectRes update(Long id, WaterProjectUpdateReq request);

    WaterProjectRes getById(Long id);

    List<WaterProjectRes> getAll();

    void delete(Long id);
}
