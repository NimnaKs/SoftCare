package me.nimnakse.water_management.tariffs.service.impl;

import java.util.List;
import me.nimnakse.water_management.tariffs.entity.MeterStatus;
import me.nimnakse.water_management.tariffs.repository.MeterStatusRepository;
import me.nimnakse.water_management.tariffs.service.MeterStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeterStatusServiceImpl implements MeterStatusService {
    private final MeterStatusRepository repository;

    public MeterStatusServiceImpl(MeterStatusRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MeterStatus> listByOrgUnitId(Long orgUnitId) {
        List<MeterStatus> statuses = repository.findByOrgUnitIdOrderByWorkflowIdAsc(orgUnitId);
        if (statuses.isEmpty()) {
            initializeDefaultStatuses(orgUnitId);
            return repository.findByOrgUnitIdOrderByWorkflowIdAsc(orgUnitId);
        }
        return statuses;
    }

    @Transactional
    @Override
    public void initializeDefaultStatuses(Long orgUnitId) {
        Object[][] defaults = {
                { 1, "New Supply Installation", "Installed", true },
                { 2, "Connection being migrated", "Installed", true },
                { 3, "New Meter Replacement", "Replaced", true },
                { 4, "Meter Repair", "Repaired", true },
                { 5, "Meter Reading Updates - Read", "Read", true },
                { 6, "Reading is reversed", "Reversed", true },
                { 7, "Connection is disconnected", "Uninstalled", false },
                { 8, "Reconnected", "Reinstalled", true },
                { 9, "Meter Reading Adjustment", "Adjusted", true }
        };

        for (Object[] row : defaults) {
            MeterStatus status = new MeterStatus();
            status.setOrgUnitId(orgUnitId);
            status.setWorkflowId((Integer) row[0]);
            status.setStatusName((String) row[1]);
            status.setMeterStatus((String) row[2]);
            status.setIsActive((Boolean) row[3]);
            repository.save(status);
        }
    }
}
