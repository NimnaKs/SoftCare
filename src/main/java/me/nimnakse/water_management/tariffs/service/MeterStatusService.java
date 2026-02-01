package me.nimnakse.water_management.tariffs.service;

import java.util.List;
import me.nimnakse.water_management.tariffs.entity.MeterStatus;

public interface MeterStatusService {
    List<MeterStatus> listByOrgUnitId(Long orgUnitId);

    void initializeDefaultStatuses(Long orgUnitId);
}
