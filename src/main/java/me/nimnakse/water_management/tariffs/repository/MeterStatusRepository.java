package me.nimnakse.water_management.tariffs.repository;

import java.util.List;
import me.nimnakse.water_management.tariffs.entity.MeterStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterStatusRepository extends JpaRepository<MeterStatus, Long> {
    List<MeterStatus> findByOrgUnitIdOrderByWorkflowIdAsc(Long orgUnitId);
}
